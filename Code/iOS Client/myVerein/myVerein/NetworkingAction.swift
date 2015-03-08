//
//  NetworkingAction.swift
//  myVerein
//
//  Created by Frank Steiler on 06/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import Locksmith

/// This class holds all available network interaction functions. It provides proper error handling and callbacks for each call.
class NetworkingAction {
    
    private struct NetworkingConstants {
        static let Login = "/api/login"
        static let Message = "/api/user/message"
    }
    
    /// This constant defines the amount of retries in case of being unable to log in.
    private static let maxLoginRetries = 1
    
    // MARK: - Login
    
    /// This function tries to log the user into the system using the stored credentials within the keychain. The callbacks are guaranteed to be executed on the main queue.
    class func loginAction(success: () -> (), failure: (NSError) -> ()) {
        let (currentUsername, currentPassword, _) = MVSecurity.instance().currentKeychain()
        if let username = currentUsername, password = currentPassword where !password.isEmpty && !username.isEmpty {
            loginAction(username, password: password, success: success, failure: failure)
        } else {
            failure(MVError.createError(.MVSessionLoadingError))
        }
    }
    
    /// This function tries to log the user into the system using the provided credentials. The callbacks are guaranteed to be executed on the main queue.
    class func loginAction(username: String, password: String, success: () -> (), failure: (NSError) -> ()) {
        if let session = NetworkingSessionFactory.instance() {

            let parameters = ["username": username,
                "password": password,
                "rememberMe": "on"]
            
            session.POST(NSURL(string: NetworkingConstants.Login, relativeToURL: session.baseURL)?.absoluteString,
                parameters: parameters,
                success:
                {
                    _, _ in
                    println("Successfully logged in")
                    // Executing success callback on main queue
                    dispatch_async(dispatch_get_main_queue()) {
                        success()
                    }
                },
                failure:
                {
                    _, error in
                    println("Unable to log in: \(error.localizedDescription)")
                    
                    // Invaliating the session in case the user changes the domain
                    NetworkingSessionFactory.invalidateInstance()
                    
                    // Executing failure callback on main queue
                    dispatch_async(dispatch_get_main_queue()) {
                        failure(error)
                    }
                }
            )
        } else {
            println("Unable to get networking session")
            dispatch_async(dispatch_get_main_queue()) {
                failure(MVError.createError(MVErrorCodes.MVSessionLoadingError))
            }
        }
    }
    
    // MARK: - Messages
    
    /// This function is gathering all unread messages. The callbacks are guaranteed to be executed on the main queue.
    class func messageSyncAction(success: (AnyObject) -> (), failure: (NSError?) -> ()) {
        // The private function is called with an initial retry count of 0, since it is the first try
        messageSyncAction(success, failure: failure, retryCount: 0)
    }
    
    /// This function is the private call for the messageSync function, containing a retry counter, needed by the failure handler.
    private class func messageSyncAction(success: (AnyObject) -> (), failure: (NSError) -> (), retryCount: Int) {
        if let session = NetworkingSessionFactory.instance() {
            session.GET(NSURL(string: NetworkingConstants.Message, relativeToURL: session.baseURL)?.absoluteString,
                parameters: nil,
                success:
                {
                    _, response in
                    // Executing success callback on main queue
                    dispatch_async(dispatch_get_main_queue()) {
                        success(response)
                    }
                },
                failure:
                {
                    _, error in
                    // Executing failure callback on main queue
                    self.handleRequestFailure(error, sender: self.messageSyncAction, retryCount: retryCount, initialSuccess: success, initialFailure: failure)
                }
            )
        } else {
            dispatch_async(dispatch_get_main_queue()) {
                failure(MVError.createError(.MVSessionLoadingError))
            }
        }
    }
    
    // MARK: - Internal functions
    
    /// This function is used to handle a failure during a request. If the failure is due to the fact that the user was not logged in the function is going to try to log the user in. In case of a successfully log in, the initial function is executed again. The retry count is tracking how often the request tried to re-log in, to prevent an infinite loop, in case of a forbidden resource.
    private class func handleRequestFailure(error: NSError, sender: ((AnyObject) -> (), (NSError) -> (), Int) -> (), retryCount: Int, initialSuccess: (AnyObject) -> (), initialFailure: (NSError) -> ()) {
        if error.code == 401 {
            if retryCount < maxLoginRetries {
                println("Error occured because user was not logged in")
                let newCount = retryCount + 1
                // Since the error occured because the user was not logged in, the log in function is called with the original function with all callbacks as success handler object. Concluding if the log in is successfull the original function should succeed.
                loginAction({sender(initialSuccess, initialFailure, newCount)}, failure: initialFailure)
            } else {
                println("Reached maximum amount of log in retries")
                // TODO: Implement showing of log in screen
                initialFailure(MVError.createError(.MVMaximumLoginRetriesReached))
            }
        } else {
            dispatch_async(dispatch_get_main_queue()) {
                initialFailure(error)
            }
        }
    }
}