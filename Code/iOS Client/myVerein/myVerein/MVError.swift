//
//  ErrorDomain.swift
//  myVerein
//
//  Created by Frank Steiler on 07/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

enum MVErrorCodes: Int {
    case MVKeychainEmptyError = 10,
         MVKeychainParseError = 11,
         MVSessionLoadingError = 20,
         MVMaximumLoginRetriesReached = 21
}

class MVError {
    
    static let errorDomain = "de.steilerdev.myVerein"
    
    class func createError(errorCode: MVErrorCodes) -> NSError {
        return NSError(domain: errorDomain, code: errorCode.rawValue, userInfo: getUserInfoForError(errorCode))
    }
    
    private class func getUserInfoForError(errorCode: MVErrorCodes) -> [NSObject: AnyObject]? {
        switch errorCode {
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
            default: return nil
        }
    }
}