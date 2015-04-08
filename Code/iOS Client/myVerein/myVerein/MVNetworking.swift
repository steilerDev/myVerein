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
  
  private static let logger = XCGLogger.defaultInstance()
  
  // MARK: - Internal functions
  
  /// This function performs a network action specified by its signature. If the request fails because of an unauthenticated user, the function tries to log the user in and then retries the initial action.
  private class func handleRequest(#URI: String, parameters: [String: String]?, requestMethod: HTTPMethods, retryCount: Int, success: (AnyObject) -> (), failure: (NSError) -> ()) {
    
    logger.debug("About to handle request for URI \(URI) with parameters \(parameters), request method \(requestMethod) and retry count \(retryCount)")
    
    if let session = MVNetworkingSessionFactory.instance() {
      
      let requestFunction: (String!, parameters: AnyObject!, success: ((NSURLSessionDataTask!, AnyObject!) -> ())!, failure: ((NSURLSessionDataTask!, NSError!) -> ())!) -> NSURLSessionDataTask!
      
      switch requestMethod {
      case .DELETE:
        requestFunction = session.DELETE
      case .GET:
        requestFunction = session.GET
      case .POST:
        requestFunction = session.POST
      }
      
      logger.debug("Executing request for URI \(URI) with parameters \(parameters), request method \(requestMethod) and retry count \(retryCount)")
      
      requestFunction(NSURL(string: URI, relativeToURL: session.baseURL)?.absoluteString,
        parameters: parameters,
        success:
        {
          _, response in
          XCGLogger.info("Successfully executed request (URI: \(URI), parameters \(parameters), request method \(requestMethod), retry count \(retryCount)")
          // Executing success callback
          success(response)
        },
        failure:
        {
          _, error in
          XCGLogger.warning("Failed executing request (URI: \(URI), parameters \(parameters), request method \(requestMethod), retry count \(retryCount)): \(error.localizedDescription)")
          // Handling a request error using the request failure handler. If the error was because of a 401 error, the handler is trying to log the user in before retrying
          MVNetworking.handleRequestFailure(
            error: error,
            URI: URI,
            parameters: parameters,
            requestMethod: requestMethod,
            retryCount: retryCount,
            initialSuccess: success,
            initialFailure: failure
          )
        }
      )
    } else {
      let error = MVError.createError(.MVSessionLoadingError)
      logger.warning("Unable to execute request: \(error.localizedDescription)")
      failure(error)
    }
  }
  
  /// This function is used to handle a failure during a request. If the failure is due to the fact that the user was not logged in the function is going to try to log the user in. In case of a successfully log in, the initial function is executed again. The retry count is tracking how often the request tried to re-log in, to prevent an infinite loop, in case of a forbidden resource.
  private class func handleRequestFailure(#error: NSError, URI: String, parameters: [String: String]?, requestMethod: HTTPMethods, retryCount: Int, initialSuccess: (AnyObject) -> (), initialFailure: (NSError) -> ()) {
    logger.verbose("Handling request failure (URI: \(URI), parameters \(parameters), request method \(requestMethod), retry count \(retryCount)): \(error.localizedDescription)")
    
    
    if retryCount < NetworkingConstants.MaxLoginRetries {
      logger.debug("Maximum login retries not reached yet: \(retryCount) of \(NetworkingConstants.MaxLoginRetries)")
      let newCount = retryCount + 1
      if  error.code == 401 || // If we are dealing with a simple HTTP error, the code will be 401
        error.code == -1011 && // If we are dealing with a serialization error, whose underlying error is 401 things get tricky
        error.domain == AFURLResponseSerializationErrorDomain &&
        (error.userInfo?[AFNetworkingOperationFailingURLResponseErrorKey] as? NSHTTPURLResponse)?.statusCode == 401
      {
        logger.info("Error occured because user was not logged in: \(error.localizedDescription)")
        // Since the error occured because the user was not logged in, the log in function is called with the original function with all callbacks as success handler object. Concluding if the log in is successfull the original function is called again and should succeed, otherwise the initial failure handler is executed.
        loginAction(
          success: {
            MVNetworking.handleRequest(
              URI: URI,
              parameters: parameters,
              requestMethod: requestMethod,
              retryCount: newCount,
              success: initialSuccess,
              failure: initialFailure
            )
          },
          failure: initialFailure
        )
      } else {
        logger.warning("Handling error of unknown kind: \(error.localizedDescription). Retrying.")
        MVNetworking.handleRequest(
          URI: URI,
          parameters: parameters,
          requestMethod: requestMethod,
          retryCount: newCount,
          success: initialSuccess,
          failure: initialFailure
        )
      }
      /// Todo: Exclude stuff like unavailable network connection & Handle unaccepted credentials
    } else if error.code == 401 || // If we are dealing with a simple HTTP error, the code will be 401
      error.code == -1011 && // If we are dealing with a serialization error, whose underlying error is 401 things get tricky
      error.domain == AFURLResponseSerializationErrorDomain &&
      (error.userInfo?[AFNetworkingOperationFailingURLResponseErrorKey] as? NSHTTPURLResponse)?.statusCode == 401
    {
      logger.error("Reached maximum amount of log in retries \(NetworkingConstants.MaxLoginRetries) and they occured because user credentials cannot be validated. Requesting re-login")
      (UIApplication.sharedApplication().delegate as! AppDelegate).showLoginView()
      initialFailure(MVError.createError(.MVMaximumLoginRetriesReached))
    } else {
      logger.severe("Reached maximum amount of retries \(NetworkingConstants.MaxLoginRetries)")
      initialFailure(MVError.createError(.MVMaximumLoginRetriesReached))
    }
  }
}

// MARK: - Login
/// This extension is centralizing all authorization related networking actions.
extension MVNetworking {
  /// This function tries to log the user into the system using the stored credentials within the keychain. The callbacks are guaranteed to be executed on the main queue.
  class func loginActionWithCallbackOnMainQueue(#success: () -> (), failure: (NSError) -> ()) {
    // Wrapping call backs into the marshal prefix operator, to execute them on the main queue
    loginAction(
      success: { ~>success },
      failure: { error in ~>{failure(error)} }
    )
  }
  
  /// This function tries to log the user into the system using the stored credentials within the keychain. The callbacks are not guaranteed to be executed on the main queue.
  class func loginAction(#success: () -> (), failure: (NSError) -> ()) {
    logger.verbose("Logging in using stored credentials")
    let (currentUsername, currentPassword, _) = MVSecurity.instance().currentKeychain()
    if let username = currentUsername, password = currentPassword where !password.isEmpty && !username.isEmpty {
      loginAction(username, password: password, success: success, failure: failure)
    } else {
      let error = MVError.createError(.MVSessionLoadingError)
      logger.warning("Unable to log in: \(error.localizedDescription)")
      failure(error)
    }
  }
  
  /// This function tries to log the user into the system using the provided credentials. The callbacks are guaranteed to be executed on the main queue. After a successfull log in the function checks the consitency of the application database, resets it if neccessary and starts syncing messages and user divisions.
  private class func loginAction(username: String, password: String, success: () -> (), failure: (NSError) -> ()) {
    logger.verbose("Logging in using provided parameters")
    if let session = MVNetworkingSessionFactory.instance() {
      
      let parameters = [
        NetworkingConstants.Login.Parameter.Username: username,
        NetworkingConstants.Login.Parameter.Password: password,
        NetworkingConstants.Login.Parameter.RememberMe: "on"
      ]
      
      session.POST(NSURL(string: NetworkingConstants.Login.URI, relativeToURL: session.baseURL)?.absoluteString,
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
              Defaults[MVUserDefaultsConstants.UserID] = newUserID
              Defaults[MVUserDefaultsConstants.SystemID] = newSystemID
              (UIApplication.sharedApplication().delegate as! AppDelegate).flushDatabase()
              MVNetworkingHelper.syncAllMessages()
            } else {
              MVNetworkingHelper.syncMessages()
              logger.debug("No need to reset database, because System ID and User ID did not change")
            }
            
            MVNetworkingHelper.syncUserDivision()
            logger.info("Successfully logged in")
            success()
          } else {
            logger.error("Unable to log in because reading header fields failed")
            failure(MVError.createError(.MVResponseHeaderError))
          }
        },
        failure:
        {
          _, error in
          XCGLogger.warning("Unable to log in: \(error.localizedDescription)")
          
          // Invaliating the session in case the user changes the domain
          MVNetworkingSessionFactory.invalidateInstance()
          // TODO: This should be the place to handle invalid credentials
          // Executing failure callback on main queue
          failure(error)
        }
      )
    } else {
      let error = MVError.createError(MVErrorCodes.MVSessionLoadingError)
      logger.warning("Unable to log in: \(error.localizedDescription)")
      failure(error)
    }
  }
}

// MARK: - Messages
/// This extension is centralizing all message related networking actions.
extension MVNetworking {
  /// This function is gathering all unread messages. The callbacks are not guaranteed to be executed on the main queue.
  class func messageSyncAction(#success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Started message sync action")
    handleRequest(
      URI: NetworkingConstants.Message.Sync.URI,
      parameters: nil,
      requestMethod: NetworkingConstants.Message.Sync.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
  
  /// This function is gathering all unread messages. The callbacks are not guaranteed to be executed on the main queue.
  class func allMessageSyncAction(#success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Started all messages sync action")
    handleRequest(
      URI: NetworkingConstants.Message.Sync.URI,
      parameters: [NetworkingConstants.Message.Sync.Parameter.All: "true"],
      requestMethod: NetworkingConstants.Message.Sync.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
  
  /// This function is sending a specific message to the server. The callbacks are not guaranteed to be executed on the main queue.
  class func sendMessageAction(#success: (AnyObject) -> (), failure: (NSError?) -> (), message: Message) {
    logger.verbose("Started sending message action")
    handleRequest(
      URI: NetworkingConstants.Message.Send.URI,
      parameters: [
        NetworkingConstants.Message.Send.Parameter.Content: message.content!,
        NetworkingConstants.Message.Send.Parameter.Division: message.division.id
      ],
      requestMethod: NetworkingConstants.Message.Send.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
}

// MARK: - User
/// This extension is centralizing all user related networking actions.
extension MVNetworking {
  /// This function is gathering all information available about a user. The callbacks are not guaranteed to be executed on the main queue.
  class func userSyncAction(#userId: String, success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Syncing user with id \(userId)")
    
    handleRequest(
      URI: NetworkingConstants.User.Get.URI,
      parameters: [NetworkingConstants.User.Get.Parameter.UserID : userId],
      requestMethod: NetworkingConstants.User.Get.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
}

// MARK: - Division
/// This extension is centralizing all division related networking actions.
extension MVNetworking {
  /// This function is gathering all information available about a division. The callbacks are not guaranteed to be executed on the main queue.
  class func divisionSyncAction(#divisionId: String, success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Syncing division with id \(divisionId)")
    handleRequest(
      URI: NetworkingConstants.Division.Get.URI,
      parameters: [NetworkingConstants.Division.Get.Parameter.DivisionID : divisionId],
      requestMethod: NetworkingConstants.Division.Get.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
  
  /// This function is gathering all dvisiions the user is subscribed to. The callbacks are not guaranteed to be executed on the main queue.
  class func userDivisionSyncAction(#success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Starting to sync list of divisions the user is part of")
    handleRequest(
      URI: NetworkingConstants.Division.Sync.URI,
      parameters: nil,
      requestMethod: NetworkingConstants.Division.Sync.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
}

// MARK: - Event
extension MVNetworking {
  /// This function is syncing all events changed since the last sync and updates the last synced information in the user defaults. The callbacks are not guaranteed to be executed on the main queue.
  class func eventSyncAction(#success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Starting to sync events")
    var parameters: [String: String]?
    if let lastChanged = Defaults[MVUserDefaultsConstants.LastSynced.Event].date {
      logger.debug("Last changed events \(lastChanged)")
      parameters = [NetworkingConstants.Event.Sync.Parameter.LastChanged: MVDateParser.stringFromDate(lastChanged)]
    }
    
    Defaults[MVUserDefaultsConstants.LastSynced.Event] = NSDate()
    
    handleRequest(
      URI: NetworkingConstants.Event.Sync.URI,
      parameters: parameters,
      requestMethod: NetworkingConstants.Event.Sync.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
  
  /// This function is gathering all information about the specified event. The callbacks are not guaranteed to be executed on the main queue.
  class func eventSyncAction(#eventID: String, success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Syncing event \(eventID)")
    
    handleRequest(
      URI: NetworkingConstants.Event.Sync.URI,
      parameters: [NetworkingConstants.Event.Sync.Parameter.EventID: eventID],
      requestMethod: NetworkingConstants.Event.Sync.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
  
  /// This function is sending a response of the user about a specific event to the server. The callbacks are not guaranteed to be executed on the main queue.
  class func eventResponseAction(#eventID: String, response: EventResponse, success: (AnyObject) -> (), failure: (NSError?) -> ()) {
    logger.verbose("Starting to send response \(response) for event \(eventID)")
    let parameter = [
      NetworkingConstants.Event.Response.Paramter.EventID: eventID,
      NetworkingConstants.Event.Response.Paramter.Response: response.rawValue
    ]
    
    handleRequest(
      URI: NetworkingConstants.Event.Response.URI,
      parameters: parameter,
      requestMethod: NetworkingConstants.Event.Response.Method,
      retryCount: 0,
      success: success,
      failure: failure
    )
  }
}