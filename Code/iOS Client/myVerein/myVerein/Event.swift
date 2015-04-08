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
//  Event.swift
//  This file holds all information related to the event object of the application
//

import Foundation
import CoreData

// MARK: - Pure database object, holding only information stored in database
class Event: NSManagedObject {
  
  @NSManaged var id: String
  @NSManaged var name: String?
  @NSManaged var endDate: NSDate?
  @NSManaged var startDate: NSDate?
  @NSManaged var eventDescription: String?
  @NSManaged var locationName: String?
  @NSManaged var locationLat: NSNumber?
  @NSManaged var locationLng: NSNumber?
  
  // Raw values stored in database, convenience getter and setter are provided in an extension. This values should -in general- not be accessed directly.
  @NSManaged var rawResponse: String?
  @NSManaged var rawInvitedDivision: NSSet?
  @NSManaged var rawGoingUser: NSSet?
  @NSManaged var rawMaybeUser: NSSet?
  @NSManaged var rawPendingUser: NSSet?
  @NSManaged var rawDeclinedUser: NSSet?
  
}

// MARK: - Convenience getter and setter for complex values stored in database
extension Event {
  var response: EventResponse? {
    get {
      if rawResponse != nil {
        return EventResponse(rawValue: rawResponse!)
      } else {
        return nil
      }
    }
    set {
      rawResponse = newValue?.rawValue
    }
  }
  
  var invitedDivision: NSMutableSet {
    return mutableSetValueForKey(EventConstants.InvitedDivision)
  }
  
  var goingUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.GoingUser)
  }
  
  var maybeUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.MaybeUser)
  }
  
  var pendingUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.PendingUser)
  }
  
  var declinedUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.DeclinedUser)
  }
  
}

// MARK: - MVCoreDataObject protocol functions
extension Event: MVCoreDataObject {
  var syncRequired: Bool {
    return (endDate == nil || startDate == nil || name == nil)
  }
  
  func sync() {
    MVNetworkingHelper.syncEvent(id)
  }
}

// MARK: - Enums
enum EventResponse:String {
  case Going = "GOING"
  case Mayber = "MAYBE"
  case Pending = "PENDING"
  case Decline = "DECLINE"
}

// MARK: - Constants
struct EventConstants {
  static let ClassName = "Event"
  static let InvitedDivision = "rawInvitedDivision"
  static let GoingUser = "rawGoingUser"
  static let PendingUser = "rawPendingUser"
  static let DeclinedUser = "rawDeclinedUser"
  static let MaybeUser = "rawMaybeUser"
  static let IdField = "id"
  static let BatchSize = 35
  
  struct RemoteEvent {
    static let Id = "id"
    static let Name = "name"
    static let Description = "description"
    
    static let EventAdmin = "eventAdmin"
    
    static let InvitedDivision = "invitedDivision"
    static let InvitedUser = "invitedUser"
    
    static let StartDateTime = "startDateTime"
    static let EndDateTime = "endDateTime"
    static let MultiDate = "multiDate"
    static let LastChanged = "lastChanged"
    
    struct location {
      static let Name = "location"
      static let Lat = "locationLat"
      static let Lng = "locationLng"
    }
  }
}