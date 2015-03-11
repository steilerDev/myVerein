//
//  NetworkingAction.swift
//  myVerein
//
//  Created by Frank Steiler on 06/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import Locksmith
import AFNetworking
import XCGLogger

/// This class holds all available network interaction functions. It provides proper error handling and callbacks for each call.
class MVNetworking {
    
    private static let logger = XCGLogger.defaultInstance()
    
    private struct NetworkingConstants {
        
        struct Login {
            static let URI = "/api/login"
            struct Parameter {
                static let Username = "username"
                static let Password = "password"
                static let RememberMe = "rememberMe"
            }
        }
        
        struct Message {
            static let BaseURI = "/api/user/message"
            struct Sync {
                static let URI = Message.BaseURI
                static let Method = HTTPMethods.GET
            }
        }
        
        struct User {
            static let BaseURI = "/api/user/user"
            struct Get {
                static let URI = User.BaseURI
                static let Method = HTTPMethods.GET
                struct Parameter {
                    static let UserID = "userID"
                }
            }
        }
        
        struct Division {
            static let BaseURI = "/api/user/division"
            struct Get {
                static let URI = Division.BaseURI
                static let Method = HTTPMethods.GET
                struct Parameter {
                    static let DivisionID = "divisionID"
                }
            }
            struct Sync {
                static let URI = Division.BaseURI + "/sync"
                static let Method = HTTPMethods.GET
            }
        }
        
        // This constant defines the amount of retries in case of being unable to log in.
        static let MaxLoginRetries = 2
    }
    
    private enum HTTPMethods: Printable {
        case GET
        case POST
        case DELETE
        
        var description: String {
            switch self {
                case .GET:
                    return "Get"
                case .POST:
                    return "Post"
                case .DELETE:
                    return "Delete"
            }
        }
    }
    
    // MARK: - Login
    
    /// This function tries to log the user into the system using the stored credentials within the keychain. The callbacks are guaranteed to be executed on the main queue.
    class func loginActionWithCallbackOnMainQueue(#success: () -> (), failure: (NSError) -> ()) {
        // Wrapping call backs into the marshal prefix operator, to execute them on the main queue
        loginAction(
            success: { ~>success },
            failure: { error in ~>{failure(error)} }
        )
    }
    
    /// This function tries to log the user into the system using the stored credentials within the keychain. The callbacks are not executed on the main queue
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
    
    /// This function tries to log the user into the system using the provided credentials. The callbacks are guaranteed to be executed on the main queue.
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
                    _, _ in
                    XCGLogger.info("Successfully logged in")
                    success()
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
    
    // MARK: - Messages
    
    /// This function is gathering all unread messages. The callbacks are not executed on the main queue.
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
    
    // MARK: - User
    
    /// This function is gathering all information available about a user. The callbacks are not executed on the main queue.
    class func userSyncAction(#userId: String, success: (AnyObject) -> (), failure: (NSError?) -> ()) {
        logger.verbose("Syncing user with id \(userId)")
        let parameters = [NetworkingConstants.User.Get.Parameter.UserID : userId]
        
        handleRequest(
            URI: NetworkingConstants.User.Get.URI,
            parameters: parameters,
            requestMethod: NetworkingConstants.User.Get.Method,
            retryCount: 0,
            success: success,
            failure: failure
        )
    }
    
    // MARK: - Division
    
    /// This function is gathering all information available about a user. The callbacks are not executed on the main queue.
    class func divisionSyncAction(#divisionId: String, success: (AnyObject) -> (), failure: (NSError?) -> ()) {
        logger.verbose("Syncing division with id \(divisionId)")
        let parameters = [NetworkingConstants.Division.Get.Parameter.DivisionID : divisionId]
        handleRequest(
            URI: NetworkingConstants.Division.Get.URI,
            parameters: parameters,
            requestMethod: NetworkingConstants.Division.Get.Method,
            retryCount: 0,
            success: success,
            failure: failure
        )
    }
    
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
        } else {
            logger.warning("Reached maximum amount of log in retries \(NetworkingConstants.MaxLoginRetries)")
            initialFailure(MVError.createError(.MVMaximumLoginRetriesReached))
        }
    }
}