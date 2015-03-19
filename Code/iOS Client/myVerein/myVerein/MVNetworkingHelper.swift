//
//  NetworkingHelper.swift
//  myVerein
//
//  Created by Frank Steiler on 06/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import XCGLogger

/// This function is used to centralized common networking tasks, like loading content and storing it persistent.
class MVNetworkingHelper {
  
  private static let logger = XCGLogger.defaultInstance()
  
  /// This function is used to gather all new messages for the user and store them persistent.
  class func syncMessages() {
    logger.verbose("Syncing messages")
    MVNetworking.messageSyncAction(
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseArray = response as? Array<AnyObject> {
          let messageRepository = MessageRepository()
          let (messages, error) = messageRepository.createMessagesFrom(serverResponseObject: responseArray)
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
  
  /// This function is used to load a user specified by its id and store him persistent
  class func syncDivision(divisionId: String) {
    logger.verbose("Syncing division with ID \(divisionId)")
    MVNetworking.divisionSyncAction(
      divisionId: divisionId,
      success: {
        response in
        let logger = XCGLogger.defaultInstance()
        if let responseDict = response as? [String: AnyObject] {
          let divisionRepository = DivisionRepository()
          let (user, error) = divisionRepository.syncDivisionWith(serverResponseObject: responseDict)
          if user == nil && error != nil {
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