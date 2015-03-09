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
                    let (messages, error) = messageRepository.parseMessageFrom(serverResponseObject: responseArray)
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
                if let responseDict = response as? Dictionary<String, AnyObject> {
                    let userRepository = UserRepository()
                    let (user, error) = userRepository.parseUserFrom(serverResponseObject: responseDict)
                    if user == nil && error != nil {
                        logger.warning("Unable to sync user \(error?.localizedDescription)")
                    } else {
                        userRepository.save()
                        logger.info("Successfully saved user")
                    }
                } else {
                    let error = MVError.createError(.MVUserCreationError, failureReason: "Unable to parse response dictionary", underlyingError: .MVServerResponseParseError)
                    logger.warning("Unable to sync messages: \(error.localizedDescription)")
                }
            },
            failure: {
                error in
                XCGLogger.warning("Unable to sync user \(userId): \(error?.localizedDescription)")
            }
        )
    }
}