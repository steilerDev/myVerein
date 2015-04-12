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
//  MVNetworkingSupportingClasses.swift
//  This file holds several helper needed by the networking operations of this application. 
//  This includes helper classes to parse objects, the API constants struct and HTTP request method enumerations.
//

import Foundation
import XCGLogger


// MARK: - Networking parser helper
class MVDateParser {
  
  private static let logger = XCGLogger.defaultInstance()
  
  struct TimestampConstants {
    static let DayOfMonth = "dayOfMonth"
    static let Month = "monthValue"
    static let Year = "year"
    static let Hour = "hour"
    static let Minute = "minute"
    static let Second = "second"
    static let NanoSecond = "nano"
  }
  
  /// This function parse the response object of a (Java 8) LocalDateTime to a NSDate. The object is nil if a parse error occured.
  class func parseDateTime(responseObject: [String: AnyObject]?) -> NSDate? {
    
    logger.verbose("Parsing DateTime: \(responseObject)")
    
    var dateComponent = NSDateComponents()
    
    if let response = responseObject,
      dayOfMonth = response[TimestampConstants.DayOfMonth] as? Int,
      month = response[TimestampConstants.Month] as? Int,
      year = response[TimestampConstants.Year] as? Int,
      hour = response[TimestampConstants.Hour] as? Int,
      minute = response[TimestampConstants.Minute] as? Int,
      second = response[TimestampConstants.Second] as? Int,
      nanoSecond = response[TimestampConstants.NanoSecond] as? Int
    {
      dateComponent.day = dayOfMonth
      dateComponent.month = month
      dateComponent.year = year
      dateComponent.hour = hour
      dateComponent.minute = minute
      dateComponent.second = second
      dateComponent.nanosecond = nanoSecond
      logger.info("Successfully parsed DateTime data")
      return NSCalendar.currentCalendar().dateFromComponents(dateComponent)
    } else {
      logger.warning("Unable to parse DateTime data: \(responseObject)")
      return nil
    }
  }
  
  /// This function parse the response object of a (Java 8) LocalDate to a NSDate. The object is nil if a parse error occured.
  class func parseDate(responseObject: [String: AnyObject]?) -> NSDate? {
    
    logger.verbose("Parsing Date: \(responseObject)")
    
    var dateComponent = NSDateComponents()
    
    if let response = responseObject,
      dayOfMonth = response[TimestampConstants.DayOfMonth] as? Int,
      month = response[TimestampConstants.Month] as? Int,
      year = response[TimestampConstants.Year] as? Int
    {
      dateComponent.day = dayOfMonth
      dateComponent.month = month
      dateComponent.year = year
      logger.info("Successfully parsed Date data")
      return NSCalendar.currentCalendar().dateFromComponents(dateComponent)
    } else {
      logger.warning("Unable to parse Date data: \(responseObject)")
      return nil
    }
  }
  
  class func stringFromDate(date: NSDate) -> String {
    logger.verbose("Converting \(date) to string")
    let dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss"
    let dateString = dateFormatter.stringFromDate(date)
    logger.debug("Converted \(date) to \(dateString)")
    return dateString
  }
}


// MARK: - Networking constants
public struct NetworkingConstants {
  
  struct Login {
    static let URI = "/api/login"
    struct Parameter {
      static let Username = "username"
      static let Password = "password"
      static let RememberMe = "rememberMe"
    }
    struct ResponseHeaderFields {
      static let SystemID = "System-ID"
      static let UserID = "User-ID"
      static let SystemVersion = "System-Version"
    }
  }
  
  struct Message {
    static let BaseURI = "/api/user/message"
    struct Sync {
      static let URI = Message.BaseURI
      static let Method = HTTPMethods.GET
      struct Parameter {
        static let All = "all"
      }
    }
    struct Send {
      static let URI = Message.BaseURI
      static let Method = HTTPMethods.POST
      struct Parameter {
        static let Content = "content"
        static let Division = "division"
      }
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
  
  struct Event {
    static let BaseURI = "/api/user/event"
    struct Get {
      static let URI = Event.BaseURI
      static let Method = HTTPMethods.GET
      struct Parameter {
        static let EventID = "id"
      }
    }
    struct Sync {
      static let URI = Event.BaseURI
      static let Method = HTTPMethods.GET
      struct Parameter {
        static let LastChanged = "lastChanged"
      }
    }
    struct Response {
      struct Send {
        static let URI = Event.BaseURI
        static let Method = HTTPMethods.POST
        struct Paramter {
          static let EventID = "id"
          static let Response = "response"
        }
      }
      struct Get {
        static let URI = Event.BaseURI
        static let Method = HTTPMethods.GET
        struct Paramter {
          static let EventID = "id"
          static let Response = "response"
        }
      }
    }
  }
  
  // This constant defines the amount of retries in case of being unable to log in.
  static let MaxLoginRetries = 2
}

// MARK: - Networking enumerations
public enum HTTPMethods: Printable {
  case GET
  case POST
  case DELETE
  
  public var description: String {
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