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
                println("=================")
                println(response)
                println("=================")
            },
            failure:
            {
                error in
                println("Unable to sync messages: \(error!)")
            }
        )
    }
}