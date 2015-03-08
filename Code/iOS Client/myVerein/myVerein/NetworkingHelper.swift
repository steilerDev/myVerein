//
//  NetworkingHelper.swift
//  myVerein
//
//  Created by Frank Steiler on 06/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

/// This function is used to centralized common networking tasks, like loading content and storing it persistent.
class NetworkingHelper {
    
    /// This function is used to gather all new messages for the user and store them persistent.
    class func syncMessages() {
        println("Syncing messages")
        NetworkingAction.messageSyncAction(
            {
                response in
                if let responseArray = response as? Array<AnyObject> {
                    let messageRepository = MessageRepository()
                    let (messages, error) = messageRepository.createMessage(responseArray)
                    if messages == nil && error != nil {
                        println("Unable to sync messages \(error?.localizedDescription)")
                    } else {
                        messageRepository.save()
                        println("Successfully saved messages: \(messages!)")
                    }
                }
            },
            failure:
            {
                error in
                println("Unable to sync messages: \(error?.localizedDescription)")
            }
        )
    }
}