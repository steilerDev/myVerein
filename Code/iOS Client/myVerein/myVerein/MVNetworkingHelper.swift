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
import CoreData

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
    MVNetworking.instance.messageSyncUnreadAction(
      success: {
        response in
        {
          (context: NSManagedObjectContext) in
          let logger = XCGLogger.defaultInstance()
          if let responseArray = response as? [AnyObject] {
            let messageRepository = MessageRepository(inContext: context)
            let (messages: [Message]?, error) = messageRepository.getOrCreateUsingArray(responseArray, AndSync: true)
            if messages == nil || error != nil {
              logger.error("Unable to sync messages \(error!.extendedDescription)")
            } else if let messages = messages where !messages.isEmpty {
              messageRepository.save()
              logger.info("Successfully synced and saved new messages")
            } else {
              logger.warning("Successfully synced messages, but there are no new messages available")
            }
          } else {
            let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response array", underlyingError: .MVServerResponseParseError)
            logger.warning("Unable to sync messages: \(error.extendedDescription)")
          }
        }~>
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
    MVNetworking.instance.messageSyncAllAction(
      success: {
        response in
        {
          (context: NSManagedObjectContext) in
          let logger = XCGLogger.defaultInstance()
          if let responseArray = response as? [AnyObject] {
            let messageRepository = MessageRepository(inContext: context)
            let (messages: [Message]?, error) = messageRepository.getOrCreateUsingArray(responseArray, AndSync: true)
            if messages == nil || error != nil {
              logger.warning("Unable to sync all messages \(error!.extendedDescription)")
            } else if let messages = messages where !messages.isEmpty {
              messageRepository.save()
              logger.info("Successfully synced and saved all messages")
            } else {
              logger.warning("Successfully synced messages, but there are no messages available")
            }
          } else {
            let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response array", underlyingError: .MVServerResponseParseError)
            logger.warning("Unable to sync all messages: \(error.extendedDescription)")
          }
        }~>
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync all messages: \(error.extendedDescription)")
      }
    )
  }
  
  /// This function gets all information about the specified message and parses it
  class func syncMessage(message: Message) {
    if !message.syncInProgress {
      logger.debug("Sync not in progress, syncing message \(message)")
      message.syncInProgress = true
      
      MVNetworking.instance.messageSyncOneAction(message.id,
        success: {
          response in
          {
            (context: NSManagedObjectContext) in
            let logger = XCGLogger.defaultInstance()
            if let responseDict = response as? [String: AnyObject] {
              let messageRepository = MessageRepository(inContext: context)
              let (message: Message?, error) = messageRepository.syncObjectUsingDictionary(responseDict)
              if message == nil || error != nil {
                logger.warning("Unable to sync message \(message): \(error!.extendedDescription)")
              } else if messageRepository.databaseDidChange {
                messageRepository.save()
                MVNotification.sendMessageSyncCompletedNotificationForNewMessage(message!)
                if let alertObject = MVDropdownAlertObject(message: message!) {
                  MVDropdownAlertCenter.instance.showNotification(alertObject)
                }
                logger.info("Successfully saved message \(message)")
              } else {
                logger.info("No need to save database or notify subscriber, because data model did not change")
              }
            } else {
              let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
              logger.warning("Unable to sync message \(message): \(error.extendedDescription)")
            }
            message.syncInProgress = false
          }~>
        },
        failure: {
          error in
          XCGLogger.warning("Unable to sync message: \(error.extendedDescription)")
          message.syncInProgress = false
        }
      )
    } else {
      logger.debug("Sync in progress, not syncing message \(message)")
    }
  }
  
  /// This function is used to send a specific message. The temporarily created ID is replaced by the system's id, if the request was successful.
  class func sendMessage(message: Message) {
    logger.debug("Sending message \(message)")
    MVNetworking.instance.sendMessageAction(message,
      success: {
        response in
        {
          (context: NSManagedObjectContext) in
          let logger = XCGLogger.defaultInstance()
          if let responseDict = response as? [String: AnyObject],
            responseId = responseDict[MessageConstants.RemoteMessage.Id] as? String,
            responseTimestampDict = responseDict[MessageConstants.RemoteMessage.Timestamp] as? [String: AnyObject],
            responseTimestamp = MVDateParser.parseDateTime(responseTimestampDict)
          {
            logger.debug("Updating message id from \(message.id) to \(responseId), and timestamp from \(message.timestamp) to \(responseTimestamp)")
            let messageRepository = MessageRepository(inContext: context)
            message.id = responseId
            message.timestamp = responseTimestamp
            messageRepository.save()
          }
        }~>
      },
      failure: {
        error in
        XCGLogger.warning("Unable to send message: \(error.extendedDescription)")
        // TODO: Implement some fallback queue that keeps on trying
      }
    )
  }
}

// MARK: - User
/// This extension is centralizing all user related networking tasks in a user friendly way, as well as handling interactions with the persistent store.
extension MVNetworkingHelper {
  /// This function is used to load a user specified by its id and store him persistent
  class func syncUser(user: User) {
    if !user.syncInProgress {
      logger.debug("Sync not in progress, syncing user \(user)")
      user.syncInProgress = true
      
      MVNetworking.instance.userSyncAction(
        userId: user.id,
        success: {
          response in
          {
            (context: NSManagedObjectContext) in
            let logger = XCGLogger.defaultInstance()
            if let responseDict = response as? [String: AnyObject] {
              let userRepository = UserRepository(inContext: context)
              let (user: User?, error) = userRepository.syncObjectUsingDictionary(responseDict)
              if user == nil || error != nil {
                logger.warning("Unable to sync user \(user): \(error!.extendedDescription)")
              } else {
                userRepository.save()
                logger.info("Successfully saved user \(user)")
              }
            } else {
              let error = MVError.createError(.MVUserCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
              logger.warning("Unable to sync user \(user): \(error.extendedDescription)")
            }
            user.syncInProgress = false
          }~>
        },
        failure: {
          error in
          XCGLogger.warning("Unable to sync user: \(error.extendedDescription)")
          user.syncInProgress = false
        }
      )
    } else {
      logger.debug("Sync in progress, not syncing user \(user)")
    }
  }
}

// MARK: - Division
/// This extension is centralizing all division related networking tasks in a user friendly way, as well as handling interactions with the persistent store.
extension MVNetworkingHelper {
  /// This function is used to load a division specified by its id and store it persistent
  class func syncDivision(division: Division) {
    if !division.syncInProgress {
      logger.debug("Sync not in progress, syncing division \(division)")
      division.syncInProgress = true
      
      MVNetworking.instance.divisionSyncAction(
        divisionId: division.id,
        success: {
          response in
          {
            (context: NSManagedObjectContext) in
            let logger = XCGLogger.defaultInstance()
            if let responseDict = response as? [String: AnyObject] {
              let divisionRepository = DivisionRepository(inContext: context)
              let (division: Division?, error) = divisionRepository.syncObjectUsingDictionary(responseDict)
              if division == nil || error != nil {
                logger.warning("Unable to sync division \(division): \(error!.extendedDescription)")
              } else {
                divisionRepository.save()
                logger.info("Successfully saved division \(division)")
              }
            } else {
              let error = MVError.createError(.MVDivisionCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
              logger.warning("Unable to sync division \(division): \(error.extendedDescription)")
            }
            division.syncInProgress = false
            }~>
        },
        failure: {
          error in
          XCGLogger.warning("Unable to sync division: \(error.extendedDescription)")
          division.syncInProgress = false
        }
      )
    } else {
      logger.debug("Sync in progress, not syncing division \(division)")
    }
  }
  
  /// This function is used to update the list of divisions the user is part of. If this list changes the suitable notifications are send.
  class func syncUserDivision() {
    logger.verbose("Syncing list of divisions the user is part of")
    MVNetworking.instance.userDivisionSyncAction(
      success: {
        response in
        {
          (context: NSManagedObjectContext) in
          let logger = XCGLogger.defaultInstance()
          if let responseArray = response as? [AnyObject] {
            let divisionRepository = DivisionRepository(inContext: context)
            let (newDivisions: [Division]?, error) = divisionRepository.getOrCreateUsingArray(responseArray, AndSync: true)
            let oldDivisions = divisionRepository.findDivisionByUserMembershipStatus(.Member) ?? [Division]()
            
            if error != nil || newDivisions == nil {
              logger.warning("Unable to sync user's divisions: \(error!.extendedDescription)")
            } else if let newDivisions = newDivisions {
              // If a former subscribed division is no longer part of the list it needs to be removed
              let formerMembers = oldDivisions.filter {!contains(newDivisions, $0)}
              // New members were not part of the old divisions
              let newMembers = newDivisions.filter {!contains(oldDivisions, $0)}
              
              if !formerMembers.isEmpty || !newMembers.isEmpty {
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
        }~>
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
  class func syncUserEvent(callback: (() -> ())? = nil) {
    logger.verbose("Syncing events for user")
    MVNetworking.instance.eventSyncAction(
      success: {
        response in
        {
          (context: NSManagedObjectContext) in
          let logger = XCGLogger.defaultInstance()
          if let responseArray = response as? [AnyObject] {
            let eventRepository = EventRepository(inContext: context)
            let (events: [Event]?, error) = eventRepository.getOrCreateUsingArray(responseArray, AndSync: false) //Force sync later anyway
            if events == nil || error != nil {
              logger.warning("Unable to sync events \(error!.extendedDescription)")
            } else if let events = events where !events.isEmpty {
              for event in events {
                // Now we know which events changed, now we need to pull the content
                MVNetworkingHelper.syncEvent(event)
              }
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
        }~>
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
  class func syncEvent(event: Event) {
    if !event.syncInProgress {
      event.syncInProgress = true
      logger.debug("Sync not in progress, syncing event \(event)")
      MVNetworking.instance.eventSyncAction(
        eventID: event.id,
        success: {
          response in
          {
            (context: NSManagedObjectContext) in
            let logger = XCGLogger.defaultInstance()
            if let responseDict = response as? [String: AnyObject] {
              let eventRepository = EventRepository(inContext: context)
              let (event: Event?, error) = eventRepository.syncObjectUsingDictionary(responseDict)
              if event == nil || error != nil {
                logger.warning("Unable to sync event \(event): \(error!.extendedDescription)")
              } else if eventRepository.databaseDidChange {
                eventRepository.save()
                MVNotification.sendCalendarSyncCompletedNotificationForChangedEvent(event!)
                logger.info("Successfully saved event \(event)")
              } else {
                logger.info("No need to save database or notify subscriber, because data model did not change")
              }
            } else {
              let error = MVError.createError(.MVEventCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
              logger.warning("Unable to sync event \(event): \(error.extendedDescription)")
            }
            event.syncInProgress = false
          }~>
        },
        failure: {
          error in
          XCGLogger.warning("Unable to sync event: \(error.extendedDescription)")
          event.syncInProgress = false
        }
      )
    } else {
      logger.debug("Sync in progress, not syncing event \(event)")
    }
  }
  
  class func sendEventResponse(event: Event) {
    if let response = event.response {
      logger.verbose("Sending response for event \(event): \(event.response)")
      MVNetworking.instance.eventSendResponseAction(
        eventID: event.id,
        response: response,
        success: {
          _ in
          XCGLogger.info("Successfully send response for event \(event), saving to database")
          EventRepository().save()
        },
        failure: {
          error in
          XCGLogger.error("Unable to send response for event: \(error.extendedDescription)")
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
    MVNetworking.instance.updateDeviceTokenAction(
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