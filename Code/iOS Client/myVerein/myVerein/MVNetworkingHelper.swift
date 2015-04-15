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
//  NetworkingHelper.swift
//  This file provides an extra layer of abstraction for the networking actions.
//  It provides class functions allowing to gather, update and store objects from the server.
//

import Foundation
import XCGLogger
import SwiftyUserDefaults

/// This class is used to centralized common networking tasks, like loading content and storing it persistent.
/// The modules for each part is added via an extension.
class MVNetworkingHelper {
  private static let logger = XCGLogger.defaultInstance()
}

// MARK: - Messages
/// This extension is centralizing all message related networking tasks in a user friendly way, as well as handling interactions with the persistent store and notification interface.
extension MVNetworkingHelper {
  /// This function is used to gather all new messages for the user and store them persistent. If there are new messages the suitable notifications are send.
  class func syncMessages() {
    logger.verbose("Syncing messages")
    MVNetworking.messageSyncAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseArray = response as? Array<AnyObject> {
          let messageRepository = MessageRepository()
          let (messages, error) = messageRepository.getOrCreateMessagesFrom(serverResponseObject: responseArray)
          if messages == nil && error != nil {
            logger.error("Unable to sync messages \(error!.extendedDescription)")
          } else if let messages = messages where !messages.isEmpty {
            messageRepository.save()
            MVNotification.sendMessageSyncCompletedNotificationForNewMessages(messages)
            logger.info("Successfully synced and saved new messages")
          } else {
            logger.warning("Successfully synced messages, but there are no new messages available")
          }
        } else {
          let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response array", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync messages: \(error.extendedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync messages: \(error.extendedDescription)")
      }
    )
  }
  
  /// This function is used to gather all messages for the user and store them persistent. If there are messages the suitable notifications are send.
  class func syncAllMessages() {
    logger.verbose("Syncing all messages")
    MVNetworking.allMessageSyncAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseArray = response as? Array<AnyObject> {
          let messageRepository = MessageRepository()
          let (messages, error) = messageRepository.getOrCreateMessagesFrom(serverResponseObject: responseArray)
          if messages == nil && error != nil {
            logger.warning("Unable to sync all messages \(error!.extendedDescription)")
          } else if let messages = messages where !messages.isEmpty {
            messageRepository.save()
            MVNotification.sendMessageSyncCompletedNotificationForNewMessages(messages)
            logger.info("Successfully synced and saved all messages")
          } else {
            logger.warning("Successfully synced messages, but there are no messages available")
          }
        } else {
          let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response array", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync all messages: \(error.extendedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync all messages: \(error.extendedDescription)")
      }
    )
  }
  
  /// This functino is used to send a specific message. The temporarily created ID is replaced by the system's id, if the request was successful.
  class func sendMessage(message: Message) {
    println("Sending message")
    MVNetworking.sendMessageAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseDict = response as? [String: AnyObject],
          responseId = responseDict[MessageConstants.RemoteMessage.Id] as? String
        {
          logger.debug("Updating message id from \(message.id) to \(responseId)")
          let messageRepository = MessageRepository()
          message.id = responseId
          messageRepository.save()
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to send message: \(error.extendedDescription)")
        // TODO: Implement some fallback queue that keeps on trying
      },
      message: message
    )
  }
}

// MARK: - User
/// This extension is centralizing all user related networking tasks in a user friendly way, as well as handling interactions with the persistent store.
extension MVNetworkingHelper {
  /// This function is used to load a user specified by its id and store him persistent
  class func syncUser(userId: String) {
    logger.verbose("Syncing user with ID \(userId)")
    MVNetworking.userSyncAction(
      userId: userId,
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseDict = response as? [String: AnyObject] {
          let userRepository = UserRepository()
          let (user, error) = userRepository.syncUserWith(serverResponseObject: responseDict)
          if user == nil && error != nil {
            logger.warning("Unable to sync user \(userId): \(error!.extendedDescription)")
          } else {
            userRepository.save()
            logger.info("Successfully saved user \(userId)")
          }
        } else {
          let error = MVError.createError(.MVUserCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync user \(userId): \(error.extendedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync user \(userId): \(error.extendedDescription)")
      }
    )
  }
}

// MARK: - Division
/// This extension is centralizing all division related networking tasks in a user friendly way, as well as handling interactions with the persistent store.
extension MVNetworkingHelper {
  /// This function is used to load a division specified by its id and store it persistent
  class func syncDivision(divisionId: String) {
    logger.verbose("Syncing division with ID \(divisionId)")
    MVNetworking.divisionSyncAction(
      divisionId: divisionId,
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseDict = response as? [String: AnyObject] {
          let divisionRepository = DivisionRepository()
          let (division, error) = divisionRepository.syncDivisionWith(serverResponseObject: responseDict)
          if division == nil && error != nil {
            logger.warning("Unable to sync division \(divisionId): \(error!.extendedDescription)")
          } else {
            divisionRepository.save()
            logger.info("Successfully saved division \(divisionId)")
          }
        } else {
          let error = MVError.createError(.MVDivisionCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync division \(divisionId): \(error.extendedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync division \(divisionId): \(error.extendedDescription)")
      }
    )
  }
  
  /// This function is used to update the list of divisions the user is part of. If this list changes the suitable notifications are send.
  class func syncUserDivision() {
    logger.verbose("Syncing list of divisions the user is part of")
    MVNetworking.userDivisionSyncAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseArray = response as? [AnyObject] {
          let divisionRepository = DivisionRepository()
          let (newDivisions, error) = divisionRepository.getOrCreateDivisionsFrom(serverResponseObject: responseArray)
          let oldDivisions = divisionRepository.findDivisionBy(userMembershipStatus: .Member) ?? [Division]()
          
          if error != nil || newDivisions == nil {
            logger.warning("Unable to sync user's divisions: \(error!.extendedDescription)")
          } else if let newDivisions = newDivisions {
            // If a former subscribed division is no longer part of the list it needs to be removed
            let formerMembers = oldDivisions.filter {!contains(newDivisions, $0)}
            // New members were not part of the old divisions
            let newMembers = newDivisions.filter {!contains(oldDivisions, $0)}
            
            if !formerMembers.isEmpty && !newMembers.isEmpty {
              logger.info("Division structure changed, applying changes")
              for division in formerMembers {
                division.userMembershipStatus = .FormerMember
              }
              
              for division in newMembers {
                division.userMembershipStatus = .Member
              }
              
              divisionRepository.save()
              MVNotification.sendDivisionSyncCompletedNotificationForChangedDivisions(formerMembers + newMembers)
              logger.info("Successfully synced user's divisions")
            } else {
              logger.info("Division structure did not change, nothing to do")
            }
          } else {
            logger.severe("Unexpected behaviour while syncing user's divisions")
          }
        } else {
          let error = MVError.createError(.MVServerResponseParseError)
          logger.warning("Unable to sync user's divisions: \(error.extendedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync user's divisions: \(error.extendedDescription)")
      }
    )
  }
}

// MARK: - Events
/// This extension is centralizing all event related networking tasks in a user friendly way, as well as handling interactions with the persistent store.
extension MVNetworkingHelper {
  
  /// This function is gathering all events that changed since the last time, the user synced his events. If the user never synced his events all events are synced. The callback function is optional and guaranteed to be executed on the main thread. If there any event changed the suitable notifications are send.
  class func syncUserEvent(callback: (() -> ())?) {
    logger.verbose("Syncing events for user")
    MVNetworking.eventSyncAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseArray = response as? [AnyObject] {
          let eventRepository = EventRepository()
          let (events, error) = eventRepository.getOrCreateEventsFrom(serverResponseObject: responseArray)
          if events == nil && error != nil {
            logger.warning("Unable to sync events \(error!.extendedDescription)")
          } else if let events = events where !events.isEmpty {
            for event in events {
              // Now we know which events changed, now we need to pull the content
              MVNetworkingHelper.syncEvent(event.id)
            }
            // Checking if database changed because it is likely that there are new events but these dont have changed at all because of the sync offset
            eventRepository.save()
            logger.info("Successfully synced and saved events")
          }  else {
            logger.warning("Successfully synced events, but there are no new messages available")
          }
        } else {
          let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response array", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync events: \(error.extendedDescription)")
        }
        
        if let callback = callback {
          logger.debug("Callback available, executing on main queue")
          ~>callback
        } else {
          logger.info("No callback available")
        }
      },
      failure: {
        error in
        let logger = XCGLogger.defaultInstance()
        logger.warning("Unable to sync user's events: \(error.extendedDescription)")
        if let callback = callback {
          logger.debug("Callback available, executing on main queue")
          ~>callback
        } else {
          logger.info("No callback available")
        }
      }
    )
  }
  
  /// This function is syncing an event with the specified id.
  class func syncEvent(eventId: String) {
    logger.verbose("Syncing event with ID \(eventId)")
    MVNetworking.eventSyncAction(
      eventID: eventId,
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseDict = response as? [String: AnyObject] {
          let eventRepository = EventRepository()
          let (event, error) = eventRepository.syncEventWith(serverResponseObject: responseDict)
          if event == nil && error != nil {
            logger.warning("Unable to sync event \(eventId): \(error!.extendedDescription)")
          } else if eventRepository.databaseDidChange {
            eventRepository.save()
            MVNotification.sendCalendarSyncCompletedNotificationForChangedEvents([event!])
            logger.info("Successfully saved event \(eventId)")
          }
        } else {
          let error = MVError.createError(.MVEventCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync event \(eventId): \(error.extendedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to event division \(eventId): \(error.extendedDescription)")
      }
    )
  }
  
  class func sendEventResponse(event: Event) {
    if let response = event.response {
      logger.verbose("Sending response for event \(event): \(event.response)")
      MVNetworking.eventSendResponseAction(
        eventID: event.id,
        response: response,
        success: {
          _ in
          XCGLogger.info("Successfully send response for event \(event), saving to database")
          EventRepository().save()
        },
        failure: {
          error in
          XCGLogger.error("Unable to send response for event \(event): \(error.extendedDescription)")
        }
      )
    } else {
      logger.error("Unable to send response for event \(event), because there is no event response")
    }
  }
}

// MARK: - Remote notifications
/// This extension handles the registration for remote notifications
extension MVNetworkingHelper {
  class func updateDeviceToken(deviceToken: NSData) {
    let encodedToken = deviceToken.base64EncodedStringWithOptions(nil)
    Defaults[MVUserDefaultsConstants.DeviceToken] = encodedToken
    MVNetworking.updateDeviceTokenAction(
      encodedToken,
      success: {
        _ in
        XCGLogger.debug("Successfully updated device token")
      },
      failure: {
        error in
        XCGLogger.error("Unable to update device token: \(error.extendedDescription)")
      }
    )
  }
}