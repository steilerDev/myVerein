//
//  ErrorDomain.swift
//  myVerein
//
//  Created by Frank Steiler on 07/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

enum MVErrorCodes: Int {
    case MVSecurityError = 10
    case MVKeychainEmptyError = 11
    case MVKeychainParseError = 12
    case MVNetworkingError = 20
    case MVSessionLoadingError = 21
    case MVMaximumLoginRetriesReached = 22
    case MVServerResponseParseError = 23
    case MVEntityCreationError = 60
    case MVDivisionCreationError = 61
    case MVMessageCreationError = 62
    case MVUserCreationError = 63
}

class MVError {
    
    static let errorDomain = "de.steilerdev.myVerein"
    
    class func createError(errorCode: MVErrorCodes) -> NSError {
        return createError(errorCode, failureReason: nil, underlyingError: nil)
    }
    
    class func createError(errorCode: MVErrorCodes, failureReason: String?, underlyingError: MVErrorCodes) -> NSError {
        return createError(errorCode, failureReason: nil, underlyingError: createError(underlyingError))
    }
    
    class func createError(errorCode: MVErrorCodes, failureReason: String?, underlyingError: NSError?) -> NSError {
        return NSError(domain: errorDomain, code: errorCode.rawValue, userInfo: getUserInfoForError(errorCode, failureReason: failureReason, underlyingError: underlyingError))
    }
    
    private class func getUserInfoForError(errorCode: MVErrorCodes, failureReason: String?, underlyingError: NSError?) -> [NSObject: AnyObject]? {
        if var userInfo = getUserInfoForError(errorCode) {
            if let unwrappedFailureReasion = failureReason {
                userInfo[NSLocalizedFailureReasonErrorKey] = unwrappedFailureReasion
            }
            
            if let unwrappedUnderlyingError = underlyingError {
                userInfo[NSUnderlyingErrorKey] = unwrappedUnderlyingError
            }
            return userInfo
        } else {
            return nil
        }
    }
    
    private class func getUserInfoForError(errorCode: MVErrorCodes) -> [NSObject: AnyObject]? {
        switch errorCode {
            case .MVSecurityError:
                return [
                    NSLocalizedDescriptionKey: "An unknown security based error occured",
                    NSLocalizedRecoverySuggestionErrorKey: "Contact the developer"
                ]
            case .MVKeychainEmptyError:
                return [
                    NSLocalizedDescriptionKey: "Unable to find any entries for this application within the keychain",
                    NSLocalizedFailureReasonErrorKey: "The previous stored entry was either removed or there was none",
                    NSLocalizedRecoverySuggestionErrorKey: "Re-enter your credentials to store them into the keychain"
                ]
            case .MVKeychainParseError:
                return [
                    NSLocalizedDescriptionKey: "Unable to retrieve neccessary entries within the keychain",
                    NSLocalizedFailureReasonErrorKey: "The previous stored entry was either removed, renamed or there was none",
                    NSLocalizedRecoverySuggestionErrorKey: "Re-enter your credentials to store them into the keychain"
                ]
            case .MVNetworkingError:
                return [
                    NSLocalizedDescriptionKey: "An unknown networking based error occured",
                    NSLocalizedRecoverySuggestionErrorKey: "Contact the developer"
                ]
            case .MVSessionLoadingError:
                return [
                    NSLocalizedDescriptionKey: "Unable to create a valid session connection to the server",
                    NSLocalizedFailureReasonErrorKey: "Either the stated domain was unreachable or SSL pinning failed",
                    NSLocalizedRecoverySuggestionErrorKey: "Retry your previous action"
                ]
            case .MVMaximumLoginRetriesReached:
                return [
                    NSLocalizedDescriptionKey: "The HTTP request reached the maximum amount of login retries",
                    NSLocalizedFailureReasonErrorKey: "Either your credentials are wrong, or you are trying to access a protected resource",
                    NSLocalizedRecoverySuggestionErrorKey: "Check your credentials and retry"
                ]
            case .MVServerResponseParseError:
                return [
                    NSLocalizedDescriptionKey: "Unable to parse server response",
                    NSLocalizedFailureReasonErrorKey: "The server response was invalid, or the server uses an incompatible version",
                    NSLocalizedRecoverySuggestionErrorKey: "Retry your action"
                ]
            case .MVServerResponseParseError:
                return [
                    NSLocalizedDescriptionKey: "Unable to parse server response",
                    NSLocalizedFailureReasonErrorKey: "The server response was invalid, or the server uses an incompatible version",
                    NSLocalizedRecoverySuggestionErrorKey: "Retry your action"
                ]
            case .MVEntityCreationError:
                return [
                    NSLocalizedDescriptionKey: "An unknown error occured during entity creation",
                    NSLocalizedRecoverySuggestionErrorKey: "Contact the developer"
                ]
            case .MVDivisionCreationError:
                return [
                    NSLocalizedDescriptionKey: "Unable to create division entity",
                    NSLocalizedFailureReasonErrorKey: "Unknown",
                    NSLocalizedRecoverySuggestionErrorKey: "Retry your action"
                ]
            case .MVMessageCreationError:
                return [
                    NSLocalizedDescriptionKey: "Unable to create message entity",
                    NSLocalizedFailureReasonErrorKey: "Unknown",
                    NSLocalizedRecoverySuggestionErrorKey: "Retry your action"
                ]
            case .MVUserCreationError:
                return [
                    NSLocalizedDescriptionKey: "Unable to create user entity",
                    NSLocalizedFailureReasonErrorKey: "Unknown",
                    NSLocalizedRecoverySuggestionErrorKey: "Retry your action"
                ]
            default: return nil
        }
    }
}