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
import MapKit
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

// MARK: - Convenience getter and setter for complex values/relations stored in database
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
    return mutableSetValueForKey(EventConstants.RawFields.InvitedDivision)
  }
  
  var goingUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.RawFields.GoingUser)
  }
  
  var maybeUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.RawFields.MaybeUser)
  }
  
  var pendingUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.RawFields.PendingUser)
  }
  
  var declinedUser: NSMutableSet {
    return mutableSetValueForKey(EventConstants.RawFields.DeclinedUser)
  }
  
  var multiDate: Bool {
    if let startDate = startDate, endDate = endDate {
      let calendar = NSCalendar.currentCalendar()
      let startDateComponent = calendar.components(.CalendarUnitMonth | .CalendarUnitDay | .CalendarUnitYear, fromDate: startDate)
      let endDateComponent = calendar.components(.CalendarUnitMonth | .CalendarUnitDay | .CalendarUnitYear, fromDate: endDate)
      return !((startDateComponent.day == endDateComponent.day) && (startDateComponent.month == endDateComponent.month) && (startDateComponent.year == endDateComponent.year))
    } else {
      return false
    }
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

// MARK: - Printable protocol function
extension Event: Printable {
  override var description: String {
    if let startDate = startDate, endDate = endDate, name = name {
      return "Event \(name) from \(startDate) to \(endDate)"
    } else {
      return "Event \(id)"
    }
  }
}

// MARK: - Variables converting the event into text for table view
extension Event {
  /// This variable gives the title for the event (either the name or the id)
  var title: String {
    return name ?? id
  }
  
  /// This variable gives a subtitle for the event (dateString and locationString, ignoring default values)
  var subTitle: String {
    var result: String = String()
    if dateString != EventConstants.Placeholder.Time {
      result += "\(dateString) "
    }
    if locationString != EventConstants.Placeholder.Location {
      result += "@ \(locationString)"
    }
    return result
  }
  
  /// Returns a string representation of the dates. If the event is not multidate, only the formatted times are returned, otherwise startdatetime and enddatetime are returned. If the times are not provided the placeholder is returned.
  var dateString: String {
    if let startDate = startDate, endDate = endDate {
      let formatter = NSDateFormatter()
      if multiDate {
        formatter.dateStyle = .ShortStyle
        formatter.timeStyle = .ShortStyle
      } else {
        formatter.timeStyle = .ShortStyle
      }
      return "\(formatter.stringFromDate(startDate)) - \(formatter.stringFromDate(endDate))"
    } else {
      return EventConstants.Placeholder.Time
    }
  }
  
  /// Returns a string representation of the dates. The string is always giving the full event date and time. If the times are not provided the placeholder is returned.
  var dateStringLong: String {
    if let startDate = startDate, endDate = endDate {
      if multiDate {
        let formatter = NSDateFormatter()
        formatter.dateStyle = .ShortStyle
        formatter.timeStyle = .ShortStyle
        return "\(formatter.stringFromDate(startDate)) - \(formatter.stringFromDate(endDate))"
      } else {
        let timeFormatter = NSDateFormatter()
        let dateFormatter = NSDateFormatter()
        timeFormatter.timeStyle = .ShortStyle
        dateFormatter.dateStyle = .ShortStyle
        return "\(dateFormatter.stringFromDate(startDate)): \(timeFormatter.stringFromDate(startDate)) - \(timeFormatter.stringFromDate(endDate))"
      }
    } else {
      return EventConstants.Placeholder.Time
    }
  }
  
  /// Returns a string representation of the location. If the location name is not provided the placeholder is returned.
  var locationString: String {
    if let locationName = locationName {
      return locationName
    } else {
      return EventConstants.Placeholder.Location
    }
  }
}

// MARK: - MKAnnotation protocol to enable the event to be shown on a map
extension Event: MKAnnotation {
  var coordinate: CLLocationCoordinate2D {
    if let locationLat = locationLat, locationLng = locationLng {
      return CLLocationCoordinate2D(latitude: locationLat.doubleValue, longitude: locationLng.doubleValue)
    } else {
      return CLLocationCoordinate2D()
    }
  }
  
  var subtitle: String! {
    return String()
  }
}

// MARK: - Event object related enumeration
enum EventResponse: String {
  case Going = "GOING"
  case Maybe = "MAYBE"
  case Pending = "PENDING"
  case Decline = "DECLINE"
}

// MARK: - Event object related constants
struct EventConstants {
  static let ClassName = "Event"
  // This variable is defining the minimum amount of time that needs to pass until the app is updating the events of a user
  static let MinimalSecondsBetweenEventSync = 0.0
  
  // This struct defines placeholder if specific variables are not available
  struct Placeholder {
    static let Time = "No time provided"
    static let Location = "No location provided"
  }
  
  // This struct defines the names of all database columns
  struct Fields {
    static let Id = "id"
    static let EndDate = "endDate"
    static let StartDate = "startDate"
  }
  
  // This struct defines the names of all database columns/relations that should not be accessed directly
  struct RawFields {
    static let UserResponse = "rawResponse"
    static let InvitedDivision = "rawInvitedDivision"
    static let GoingUser = "rawGoingUser"
    static let PendingUser = "rawPendingUser"
    static let DeclinedUser = "rawDeclinedUser"
    static let MaybeUser = "rawMaybeUser"
  }
  
  // This struct defines the names of the member fields on the remote object. They are used to parse the event.
  struct RemoteEvent {
    static let Id = "id"
    static let Name = "name"
    static let Description = "description"
    
    static let EventAdmin = "eventAdmin"
    
    static let InvitedDivision = "invitedDivision"
    static let InvitedUser = "invitedUser"
    static let UserResponse = "userResponse"
    
    static let StartDateTime = "startDateTime"
    static let EndDateTime = "endDateTime"
    static let MultiDate = "multiDate"
    static let LastChanged = "lastChanged"
    
    struct Location {
      static let Name = "location"
      static let Lat = "locationLat"
      static let Lng = "locationLng"
    }
  }
}