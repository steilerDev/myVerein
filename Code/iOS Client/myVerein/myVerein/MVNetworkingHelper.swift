//
//  NetworkingHelper.swift
//  myVerein
//
//  Created by Frank Steiler on 06/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
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
/// This extension is centralizing all message related networking tasks in a user friendly way, as well as handling interactions with the persistent store.
extension MVNetworkingHelper {
  /// This function is used to gather all new messages for the user and store them persistent.
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
            logger.warning("Unable to sync messages \(error?.localizedDescription)")
          } else {
            messageRepository.save()
            logger.info("Successfully synced and saved messages")
          }
        } else {
          let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response array", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync messages: \(error.localizedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync messages: \(error?.localizedDescription)")
      }
    )
  }
  
  /// This function is used to gather all messages for the user and store them persistent.
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
            logger.warning("Unable to sync all messages \(error?.localizedDescription)")
          } else {
            messageRepository.save()
            logger.info("Successfully synced and saved all messages")
          }
        } else {
          let error = MVError.createError(.MVMessageCreationError, failureReason: "Unable to parse response array", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync all messages: \(error.localizedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync all messages: \(error?.localizedDescription)")
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
        XCGLogger.warning("Unable to send message: \(error?.localizedDescription)")
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
            logger.warning("Unable to sync user \(userId): \(error?.localizedDescription)")
          } else {
            userRepository.save()
            logger.info("Successfully saved user \(userId)")
          }
        } else {
          let error = MVError.createError(.MVUserCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync user \(userId): \(error.localizedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync user \(userId): \(error?.localizedDescription)")
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
            logger.warning("Unable to sync division \(divisionId): \(error?.localizedDescription)")
          } else {
            divisionRepository.save()
            logger.info("Successfully saved division \(divisionId)")
          }
        } else {
          let error = MVError.createError(.MVDivisionCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
          logger.warning("Unable to sync division \(divisionId): \(error.localizedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync division \(divisionId): \(error?.localizedDescription)")
      }
    )
  }
  
  /// This function is used to update the list of divisions the user is part of
  class func syncUserDivision() {
    logger.verbose("Syncing list of divisions the user is part of")
    MVNetworking.userDivisionSyncAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseArray = response as? [AnyObject] {
          let divisionRepository = DivisionRepository()
          let (wrappedDivisions, error) = divisionRepository.getOrCreateDivisionsFrom(serverResponseObject: responseArray)
          let oldDivisions = divisionRepository.findDivisionBy(userMembershipStatus: .Member) ?? [Division]()
          
          if error != nil || wrappedDivisions == nil {
            logger.warning("Unable to sync user's divisions: \(error?.localizedDescription)")
          } else if let currentDivisions = wrappedDivisions {
            // If a former subscribed division is no longer part of the list it needs to be removed
            let formerMembers = oldDivisions.filter {!contains(currentDivisions, $0)}
            // New members were not part of the old divisions
            let newMembers = currentDivisions.filter {!contains(oldDivisions, $0)}
            
            for division in formerMembers {
              division.userMembershipStatus = .FormerMember
            }
            
            for division in newMembers {
              division.userMembershipStatus = .Member
            }
            
            divisionRepository.save()
            logger.info("Successfully synced user's divisions")
          } else {
            logger.severe("Unexpected behaviour while syncing user's divisions")
          }
        } else {
          let error = MVError.createError(.MVServerResponseParseError)
          logger.warning("Unable to sync user's divisions: \(error.localizedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync user's divisions: \(error?.localizedDescription)")
      }
    )
  }
}

// MARK: - Events
/// This extension is centralizing all event related networking tasks in a user friendly way, as well as handling interactions with the persistent store.
extension MVNetworkingHelper {
  class func syncUserEvent() {
    logger.verbose("Syncing events for user")
    MVNetworking.eventSyncAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let response = response as? [[String: String]] {
          for event in response {
            if let eventId = event[EventConstants.RemoteEvent.Id] {
              
              
              
              MVNetworkingHelper.syncEvent(eventId)
            } else {
              logger.severe("Unable to get event id for event \(event)")
            }
          }
        } else {
          let error = MVError.createError(.MVServerResponseParseError)
          logger.severe("Unable to sync user's divisions: \(error.localizedDescription)")
        }
      },
      failure: {
        error in
        XCGLogger.warning("Unable to sync user's events: \(error?.localizedDescription)")
      }
    )
  }
  
  class func syncEvent(eventId: String) {
    
  }
}