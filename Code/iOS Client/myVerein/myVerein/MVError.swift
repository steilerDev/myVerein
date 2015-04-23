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
//  MVError.swift
//  This file holds all information related with error handling. It defines an application specific error domain with defined error codes. On top of that the creation of a new application related error is provided.
//

import Foundation
import XCGLogger
import AFNetworking

enum MVErrorCodes: Int {
  // Security related errors
  case MVSecurityError = 10
  case MVKeychainEmptyError = 11
  case MVKeychainParseError = 12
  
  // Networking related errors
  case MVNetworkingError = 20
  case MVSessionLoadingError = 21
  case MVMaximumLoginRetriesReached = 22
  case MVServerResponseParseError = 23
  case MVResponseHeaderError = 24
  case MVLastSyncTooCloseError = 25
  case MVEmptyResponse = 26
  case MVNotLoggedInError = 27
  case MVHTTPClientError = 28
  case MVHostNotReachable = 29
  case MVHostError = 30
  case MVRequestCancelled = 31
  
  // CoreData related errors
  case MVLocalDatabaseError = 50
  case MVLocalDatabaseLoadingError = 51
  
  // Response parsing errors
  case MVEntityCreationError = 60
  case MVDivisionCreationError = 61
  case MVMessageCreationError = 62
  case MVUserCreationError = 63
  case MVEventCreationError = 64
  
  // Internal errors
  case MVFunctionNotImplemented = 100
  case MVErrorNotRecognized = 101
}

class MVError {
  
  static let errorDomain = "de.steilerdev.myVerein"
  
  class func createError(errorCode: MVErrorCodes) -> NSError {
    return createError(errorCode, failureReason: nil, underlyingError: nil)
  }
  
  class func createError(errorCode: MVErrorCodes, underlyingError: NSError?) -> NSError {
    return createError(errorCode, failureReason: nil, underlyingError: underlyingError)
  }
  
  class func createError(errorCode: MVErrorCodes, failureReason: String?, underlyingError: MVErrorCodes) -> NSError {
    return createError(errorCode, failureReason: nil, underlyingError: createError(underlyingError))
  }
  
  class func createError(errorCode: MVErrorCodes, failureReason: String?, underlyingError: NSError?) -> NSError {
    return NSError(domain: errorDomain, code: errorCode.rawValue, userInfo: getUserInfoForError(errorCode, failureReason: failureReason, underlyingError: underlyingError))
  }
  
  private class func getUserInfoForError(errorCode: MVErrorCodes, failureReason: String?, underlyingError: NSError?) -> [NSObject: AnyObject]? {
    if var userInfo = getUserInfoForError(errorCode) {
      if let failureReason = failureReason {
        userInfo[NSLocalizedFailureReasonErrorKey] = failureReason
      }
      
      if let underlyingError = underlyingError {
        userInfo[NSUnderlyingErrorKey] = underlyingError
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
      case .MVLastSyncTooCloseError:
        return [
          NSLocalizedDescriptionKey: "It has not passed enough time since the last sync",
          NSLocalizedFailureReasonErrorKey: "The time between syncronization is too high",
          NSLocalizedRecoverySuggestionErrorKey: "Wait the specified amount of time and try again"
        ]
      case .MVServerResponseParseError:
        return [
          NSLocalizedDescriptionKey: "Unable to parse server response",
          NSLocalizedFailureReasonErrorKey: "The server response was invalid, or the server uses an incompatible version",
          NSLocalizedRecoverySuggestionErrorKey: "Retry your action"
        ]
      case .MVResponseHeaderError:
        return [
          NSLocalizedDescriptionKey: "Unable to read the response header fields",
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
      case .MVEventCreationError:
        return [
          NSLocalizedDescriptionKey: "Unable to create event entity",
          NSLocalizedFailureReasonErrorKey: "Unknown",
          NSLocalizedRecoverySuggestionErrorKey: "Retry your action"
        ]
      case .MVLocalDatabaseError:
        return [
          NSLocalizedDescriptionKey: "An error related to your local database ocurred",
          NSLocalizedFailureReasonErrorKey: "Unknown"
        ]
      case .MVLocalDatabaseLoadingError:
        return [
          NSLocalizedDescriptionKey: "Failed to initialize the application's saved data",
          NSLocalizedFailureReasonErrorKey: "There was an error creating or loading the application's saved data.",
          NSLocalizedRecoverySuggestionErrorKey: "If the error persists, remove the application and re-install it"
        ]
      case .MVEmptyResponse:
        return [
          NSLocalizedDescriptionKey: "The server response does not contain any data",
          NSLocalizedFailureReasonErrorKey: "In general this means that the task does not have anything to do",
          NSLocalizedRecoverySuggestionErrorKey: "If this behaviour is not an options retry your action"
        ]
      case .MVFunctionNotImplemented:
        return [
          NSLocalizedDescriptionKey: "The used function is not implemented",
          NSLocalizedFailureReasonErrorKey: "The function might be part of a base which needs to get overwritten",
          NSLocalizedRecoverySuggestionErrorKey: "Overwrite the function and re-run the task"
        ]
      case .MVNotLoggedInError:
        return [
          NSLocalizedDescriptionKey: "The user is currently not logged in",
          NSLocalizedFailureReasonErrorKey: "The session might have timed out, or the user did not log in previously",
          NSLocalizedRecoverySuggestionErrorKey: "Use the user's credentials to log in"
        ]
      case .MVHostNotReachable:
        return [
          NSLocalizedDescriptionKey: "The specified host can not be reached",
          NSLocalizedFailureReasonErrorKey: "The host is down, the internet connection is not working or the specified host does not exist",
          NSLocalizedRecoverySuggestionErrorKey: "Check your internet connection and retry"
        ]
      case .MVErrorNotRecognized:
        return [
          NSLocalizedDescriptionKey: "The provided error is unknown",
          NSLocalizedFailureReasonErrorKey: "The system is not aware of this error",
          NSLocalizedRecoverySuggestionErrorKey: "Contact the developer"
        ]
      case .MVHTTPClientError:
        return [
          NSLocalizedDescriptionKey: "The server response contained a 4xx error code",
          NSLocalizedFailureReasonErrorKey: "The clients request was faulty",
          NSLocalizedRecoverySuggestionErrorKey: "Check the request and try again"
        ]
      case .MVHostError:
        return [
          NSLocalizedDescriptionKey: "The server reported a problem",
          NSLocalizedFailureReasonErrorKey: "The request could not be handled by the server at the moment",
          NSLocalizedRecoverySuggestionErrorKey: "Check the request and try again"
        ]
      case .MVRequestCancelled:
        return [
          NSLocalizedDescriptionKey: "The request was cancelled while waiting for the response",
          NSLocalizedFailureReasonErrorKey: "The request was cancelled during execution",
          NSLocalizedRecoverySuggestionErrorKey: "Try again"
        ]
    default: return nil
    }
  }
  
  /// This function tries to map a known error from a different domains into this domain.
  ///
  /// :param: error The error that should be converted.
  /// :returns: An error in this domain. If the error is not known, the original error is wrapped into a MVErrorUnknown error.
  class func convertToMVError(error: NSError) -> NSError {
    XCGLogger.debug("Trying to convert \(error.extendedDescription)")
    if error.domain == errorDomain {
      return error
    } else if error.code == -1011 && error.domain == AFURLResponseSerializationErrorDomain {
      if let httpErrorCode = (error.userInfo?[AFNetworkingOperationFailingURLResponseErrorKey] as? NSHTTPURLResponse)?.statusCode {
        XCGLogger.debug("The HTTP error code is \(httpErrorCode)")
        switch httpErrorCode {
          case 401, 403:
           return MVError.createError(.MVNotLoggedInError, underlyingError: error)
          case 400..<500:
            return MVError.createError(.MVHTTPClientError, underlyingError: error)
          case 502, 503:
            return MVError.createError(.MVHostNotReachable, underlyingError: error)
          case 500..<600:
            return MVError.createError(.MVHostError, underlyingError: error)
          default: break
        }
      }
    } else if error.domain == "NSURLErrorDomain" {
      if error.code = -1004 {
      return MVError.createError(.MVHostNotReachable, underlyingError: error)
      } else if error.code = -999 {
        return MVError.createError(.MVRequestCancelled, underlyingError: error)
      }
    }
    return MVError.createError(.MVErrorNotRecognized, underlyingError: error)
  }
}