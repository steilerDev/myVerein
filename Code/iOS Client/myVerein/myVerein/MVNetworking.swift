//
// Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

//
//  NetworkingAction.swift
//  This file is providing the lowest level of networking actions. A unified way to handle errors and authentication is done by this class. 
//  Extensions of the class offer more abstract ways to perform the pure network interactions for gathering objects.
//

import Foundation
import Locksmith
import AFNetworking
import XCGLogger
import SwiftyUserDefaults



/// This class provides proper network and error handling with the currently stored host.
class MVNetworking {
  
  private let logger = XCGLogger.defaultInstance()
  private var session: AFHTTPSessionManager! {
    return MVNetworkingSessionFactory.defaultInstance()
  }
  
  /// Within this array all requests are stored, that could not be executed because the user was not logged in. After a successfull log in, the requests on this queue are executed. If the queue is nil, the system currently does not try to log in. Since arrays are not thread safe an external lock is used to secure the resource.
  private var logInQueue: [MVRequest]!
  private var logInQueueLock = NSLock()
  
  // MARK: - Internal functions
  
  // MARK: Singleton pattern
  
  private static var instance: MVNetworking?
  
  /// This function is returning the default networking instance.
  class func defaultInstance() -> MVNetworking {
    if instance == nil {
      XCGLogger.info("Creating new MVNetworking instance")
      instance = MVNetworking()
    }
    return instance!
  }
  
  // MARK: Request handling
  
  /// This function performs a network action specified by its signature. If the request fails because of an unauthenticated user, the function tries to log the user in and then retries the initial action.
  private func handleRequest(#URI: String, parameters: [String: String]?, requestMethod: HTTPMethods, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    handleRequest(MVRequest(URI: URI, parameters: parameters, requestMethod: requestMethod, success: success, failure: failure))
  }
  
  /// This function performs a network action specified by the request struct. If the request fails because of an unauthenticated user, the function tries to log the user in and then retries the initial action.
  private func handleRequest(request: MVRequest) {
    
    logger.debug("About to handle \(request)")
    
    if let session = session {
      
      let requestFunction = request.requestFunctionInSession(session)
      
      logger.debug("Executing \(request)")
      requestFunction(URLFromURI(request.URI),
        parameters: request.parameters,
        success:
        {
          _, response in
          XCGLogger.info("Successfully executed \(request)")
          // Executing success callback
          if response != nil {
            request.success?(response)
          } else {
            let error = MVError.createError(.MVEmptyResponse)
            XCGLogger.warning("The response for \(request) is empty, in general there is nothing to do, executing failure function anyway")
            request.failure?(error)
          }
        },
        failure:
        {
          _, error in
          XCGLogger.warning("Failed executing \(request): \(error.extendedDescription)")
          // Handling a request error using the request failure handler. If the error was because of a 401 error, the handler is trying to log the user in before retrying
          self.handleRequest(request, withError: error)
        }
      )
    } else {
      let error = MVError.createError(.MVSessionLoadingError)
      logger.warning("Unable to execute \(request): \(error.extendedDescription)")
      request.failure?(error)
    }
  }
  
  /// This function is used to handle a failure during a request. If the failure is due to the fact that the user was not logged in the function is going to try to log the user in. In case of a successfully log in, the initial function is executed again. The retry count is tracking how often the request tried again, to prevent an infinite loop, in case of a forbidden resource.
  private func handleRequest(request: MVRequest, withError error: NSError) {
    logger.verbose("Handling failure of \(request): \(error.extendedDescription)")
    
    let localError = MVError.convertToMVError(error)
    
    if localError.code == MVErrorCodes.MVNotLoggedInError.rawValue {
      logger.info("Error occured because user was not logged in: \(localError.extendedDescription)");
      handleRequestAfterLogIn(request)
    } else if localError.code == MVErrorCodes.MVHostNotReachable.rawValue {
      logger.warning("Unable to reach server. No need to retry at the moment: \(localError.extendedDescription)")
      request.failure?(localError)
      // TODO: Show error that host is not reachable
    } else {
      logger.warning("Unable to handle error: \(localError.extendedDescription)")
      request.failure?(localError)
    }
  }

  /// This function is called in case a request failed because the user was not authenticated. This function is queuing the requests that failed, while attempting to authenticate the user. If the authentication is successfull the queued requests are re-executed, otherwise their failure callback is executed. The complete function is executed asyncronously, to avoid blocking the main queue while waiting to gain posession of the shared queue.
  private func handleRequestAfterLogIn(request: MVRequest) {
    {
      self.logInQueueLock.lock()
      if self.logInQueue != nil {
        self.logger.debug("Log in currently in progress, putting request on queue")
        self.logInQueue.append(request)
        self.logInQueueLock.unlock()
      } else {
        self.logInQueue = [request]
        self.logInQueueLock.unlock()
        self.performLogIn(
          success: {
            {
              self.logInQueueLock.lock()
              let logger = XCGLogger.defaultInstance()
              logger.debug("Successfully logged in, processing queued requests")
              for request in self.logInQueue {
                self.handleRequest(request)
              }
              self.logInQueue = nil
              self.logInQueueLock.unlock()
            }~> // Executing call back on background thread, because it is possible that the log in queue is locked and would therefore eventually block the main queue.
          },
          failure: {
            error in
            {
              self.logInQueueLock.lock()
              let logger = XCGLogger.defaultInstance()
              logger.debug("Unable to log in, processing queued requests")
              for request in self.logInQueue {
                request.failure?(error)
              }
              self.logInQueue = nil
              self.logInQueueLock.unlock()
            }~> // Executing call back on background thread, because it is possible that the log in queue is locked and would therefore eventually block the main queue.
          }
        )
      }
    }~>
  }
  
  // MARK: Login
  
  /// This function tries to log the user into the system using the stored credentials within the keychain. The callbacks are not guaranteed to be executed on the main queue.
  /// After a successfull log in the function checks if the user is the same as the previously logged in user. If this is not true the function deletes the user's data on the device and starts a re-sync.
  func performLogIn(showLoginScreenOnFailure: Bool = true, success: (() -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Logging in using stored credentials")
    let (username, password, _) = MVSecurity.instance().currentKeychain()
    if let username = username, password = password where !password.isEmpty && !username.isEmpty {
      let parameters = [
        NetworkingConstants.Login.Parameter.Username: username,
        NetworkingConstants.Login.Parameter.Password: password,
        NetworkingConstants.Login.Parameter.RememberMe: "on"
      ]
    
      session.POST(URLFromURI(NetworkingConstants.Login.URI),
        parameters: parameters,
        success:
        {
          dataTask, _ in
          
          let logger = XCGLogger.defaultInstance()
          
          if let response = dataTask.response as? NSHTTPURLResponse,
            header = response.allHeaderFields as? [String: String],
            newUserID = header[NetworkingConstants.Login.ResponseHeaderFields.UserID],
            newSystemID = header[NetworkingConstants.Login.ResponseHeaderFields.SystemID],
            newSystemVersion = header[NetworkingConstants.Login.ResponseHeaderFields.SystemVersion]
          {
            logger.debug("Successfully read header fields")
            
            //TODO: Distinct system change and user change
            if !Defaults.hasKey(MVUserDefaultsConstants.UserID) ||
              Defaults[MVUserDefaultsConstants.UserID].string! != newUserID ||
              !Defaults.hasKey(MVUserDefaultsConstants.SystemID)  ||
              Defaults[MVUserDefaultsConstants.SystemID].string! != newSystemID
            {
              logger.info("System ID or User ID did change or this is the first log in. Storing and resetting database")
              logger.debug("SystemID Old: \(Defaults[MVUserDefaultsConstants.UserID].string), new: \(newUserID)")
              logger.debug("UserID Old: \(Defaults[MVUserDefaultsConstants.SystemID].string), new: \(newSystemID)")
              Defaults[MVUserDefaultsConstants.UserID] = newUserID
              Defaults[MVUserDefaultsConstants.SystemID] = newSystemID
              (UIApplication.sharedApplication().delegate as! AppDelegate).flushDatabase()
              MVNetworkingHelper.syncAllMessages()
              MVNetworkingHelper.syncUserDivision()
            } else {
              logger.debug("No need to reset database, because System ID and User ID did not change")
            }
            
            logger.info("Successfully logged in")
            success?()
          } else {
            let error = MVError.createError(.MVResponseHeaderError)
            logger.error("Unable to log in because reading header fields failed: \(error.extendedDescription)")
            failure?(error)
          }
        },
        failure:
        {
          _, error in
          XCGLogger.warning("Unable to log in: \(error.extendedDescription)")
          
          // Invaliating the session in case the user changes the domain
          MVNetworkingSessionFactory.invalidateInstance()
          if showLoginScreenOnFailure {
            ~>{ (UIApplication.sharedApplication().delegate as! AppDelegate).showLoginView() }
          }
          failure?(error)
        }
      )
    } else {
      let error = MVError.createError(.MVSessionLoadingError)
      logger.warning("Unable to log in: \(error.extendedDescription)")
      failure?(error)
    }
  }
}

// MARK: - Messages
/// This extension is centralizing all message related networking actions.
extension MVNetworking {
  /// This function is gathering all unread messages. The callbacks are not guaranteed to be executed on the main queue.
  func messageSyncUnreadAction(#success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Started message sync action")
    handleRequest(
      URI: NetworkingConstants.Message.Sync.URI,
      parameters: nil,
      requestMethod: NetworkingConstants.Message.Sync.Method,
      success: success,
      failure: failure
    )
  }
  
  /// This function is gathering all messages. The callbacks are not guaranteed to be executed on the main queue.
  func messageSyncAllAction(#success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Started all messages sync action")
    handleRequest(
      URI: NetworkingConstants.Message.Sync.URI,
      parameters: [NetworkingConstants.Message.Sync.Parameter.All: "true"],
      requestMethod: NetworkingConstants.Message.Sync.Method,
      success: success,
      failure: failure
    )
  }
  
  func messageSyncOneAction(messageId: String, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Syncing message with id \(messageId)")
    handleRequest(
      URI: NetworkingConstants.Message.Sync.URI,
      parameters: [NetworkingConstants.Message.Sync.Parameter.MessageID: messageId],
      requestMethod: NetworkingConstants.Message.Sync.Method,
      success: success,
      failure: failure
    )
  }
  
  /// This function is sending a specific message to the server. The callbacks are not guaranteed to be executed on the main queue.
  func sendMessageAction(message: Message, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Started sending message action with message \(message)")
    handleRequest(
      URI: NetworkingConstants.Message.Send.URI,
      parameters: [
        NetworkingConstants.Message.Send.Parameter.Content: message.content!,
        NetworkingConstants.Message.Send.Parameter.Division: message.division.id
      ],
      requestMethod: NetworkingConstants.Message.Send.Method,
      success: success,
      failure: failure
    )
  }
}

// MARK: - User
/// This extension is centralizing all user related networking actions.
extension MVNetworking {
  /// This function is gathering all information available about a user. The callbacks are not guaranteed to be executed on the main queue.
  func userSyncAction(#userId: String, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Syncing user with id \(userId)")
    
    handleRequest(
      URI: NetworkingConstants.User.Get.URI,
      parameters: [NetworkingConstants.User.Get.Parameter.UserID : userId],
      requestMethod: NetworkingConstants.User.Get.Method,
      success: success,
      failure: failure
    )
  }
}

// MARK: - Division
/// This extension is centralizing all division related networking actions.
extension MVNetworking {
  /// This function is gathering all information available about a division. The callbacks are not guaranteed to be executed on the main queue.
  func divisionSyncAction(#divisionId: String, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Syncing division with id \(divisionId)")
    handleRequest(
      URI: NetworkingConstants.Division.Get.URI,
      parameters: [NetworkingConstants.Division.Get.Parameter.DivisionID : divisionId],
      requestMethod: NetworkingConstants.Division.Get.Method,
      success: success,
      failure: failure
    )
  }
  
  /// This function is gathering all dvisiions the user is subscribed to. The callbacks are not guaranteed to be executed on the main queue.
  func userDivisionSyncAction(#success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Starting to sync list of divisions the user is part of")
    handleRequest(
      URI: NetworkingConstants.Division.Sync.URI,
      parameters: nil,
      requestMethod: NetworkingConstants.Division.Sync.Method,
      success: success,
      failure: failure
    )
  }
}

// MARK: - Event
extension MVNetworking {
  /// This function is syncing all events changed since the last sync and updates the last synced information in the user defaults. If the last sync is not The callbacks are not guaranteed to be executed on the main queue.
  func eventSyncAction(#success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Starting to sync events")
    var parameters: [String: String]?
    if let lastSynced = Defaults[MVUserDefaultsConstants.LastSynced.Event].date {
      logger.debug("Last synced events \(lastSynced)")
      
      // Checking if the last sync is not older than the defined MinimalSecondsBetweenEventSync and aborting the sync if so
      if NSDate().isBefore(lastSynced.dateByAddingTimeInterval(EventConstants.MinimalSecondsBetweenEventSync)) {
        let error = MVError.createError(.MVLastSyncTooCloseError)
        logger.warning("Last sync is not \(EventConstants.MinimalSecondsBetweenEventSync) seconds up. Not syncing: \(error.extendedDescription)")
        failure?(error)
        return
      } else {
        logger.debug("Last sync is longer than \(EventConstants.MinimalSecondsBetweenEventSync) seconds up")
        // Adding an offset to the last synced date, to take into account that the time of the server and the user's devices is not synchronized. Therefore removing the time defined within the constant.
        let lastSyncedWithOffset = lastSynced.dateByAddingTimeInterval(EventConstants.SyncOffsetInSeconds)
        logger.debug("Syncing with \(lastSyncedWithOffset), to compensate time differences with the server")
        parameters = [NetworkingConstants.Event.Sync.Parameter.LastChanged: MVDateParser.stringFromDate(lastSyncedWithOffset)]
      }
    }
    
    Defaults[MVUserDefaultsConstants.LastSynced.Event] = NSDate()
    
    handleRequest(
      URI: NetworkingConstants.Event.Sync.URI,
      parameters: parameters,
      requestMethod: NetworkingConstants.Event.Sync.Method,
      success: success,
      failure: failure
    )
  }
  
  /// This function is gathering all information about the specified event. The callbacks are not guaranteed to be executed on the main queue.
  func eventSyncAction(#eventID: String, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Syncing event \(eventID)")
    
    handleRequest(
      URI: NetworkingConstants.Event.Get.URI,
      parameters: [NetworkingConstants.Event.Get.Parameter.EventID: eventID],
      requestMethod: NetworkingConstants.Event.Get.Method,
      success: success,
      failure: failure
    )
  }
  
  /// This function is sending a response of the user about a specific event to the server. The callbacks are not guaranteed to be executed on the main queue.
  func eventSendResponseAction(#eventID: String, response: EventResponse, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Starting to send response \(response) for event \(eventID)")
    let parameter = [
      NetworkingConstants.Event.Response.Send.Paramter.EventID: eventID,
      NetworkingConstants.Event.Response.Send.Paramter.Response: response.rawValue
    ]
    
    handleRequest(
      URI: NetworkingConstants.Event.Response.Send.URI,
      parameters: parameter,
      requestMethod: NetworkingConstants.Event.Response.Send.Method,
      success: success,
      failure: failure
    )
  }
  
  /// This function is getting all responses of users from a specific event from the server. The callbacks are not guaranteed to be executed on the main queue.
  func eventGetResponseAction(#eventID: String, response: EventResponse, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Starting to send response \(response) for event \(eventID)")
    let parameter = [
      NetworkingConstants.Event.Response.Get.Paramter.EventID: eventID,
      NetworkingConstants.Event.Response.Get.Paramter.Response: response.rawValue
    ]
    
    handleRequest(
      URI: NetworkingConstants.Event.Response.Get.URI,
      parameters: parameter,
      requestMethod: NetworkingConstants.Event.Response.Get.Method,
      success: success,
      failure: failure
    )
  }
}

// MARK: - Remote notifications
extension MVNetworking {
  func updateDeviceTokenAction(encodedDeviceToken: String, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    logger.verbose("Updating device token \(encodedDeviceToken)")
    handleRequest(
      URI: NetworkingConstants.User.Update.DeviceToken.URI,
      parameters: [NetworkingConstants.User.Update.DeviceToken.Parameter.DeviceToken: encodedDeviceToken],
      requestMethod: NetworkingConstants.User.Update.DeviceToken.Method,
      success: success,
      failure: failure
    )
  }
}

// MARK: - Utility functions
extension MVNetworking {
  /// This function converts a given URI into the absolute URL, using the session's base URL.
  private func URLFromURI(URI: String) -> String? {
    return NSURL(string: URI, relativeToURL: session?.baseURL)?.absoluteString
  }
}