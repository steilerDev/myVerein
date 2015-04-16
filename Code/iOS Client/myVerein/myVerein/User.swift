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
//  User.swift
//  This file holds all information related to the user object of the application
//

import Foundation
import CoreData
import UIKit

// MARK: - Pure database object, holding only information stored in database
class User: NSManagedObject {
  
  @NSManaged var birthday: NSDate?
  @NSManaged var city: String?
  @NSManaged var country: String?
  @NSManaged var email: String?
  @NSManaged var firstName: String?
  @NSManaged var id: String
  @NSManaged var lastName: String?
  @NSManaged var street: String?
  @NSManaged var streetNumber: String?
  @NSManaged var zipCode: String?
  @NSManaged var lastSynced: NSDate
  
  // Raw values stored in database, convenience getter and setter are provided in an extension. This values should -in general- not be accessed directly.
  @NSManaged var rawAvatar: NSData?
  @NSManaged var rawMembershipStatus: String?
  @NSManaged var rawGender: String?
  @NSManaged private var rawAdministratedDivisions: NSSet
  @NSManaged private var rawDivisions: NSSet
  @NSManaged private var rawSendMessages: NSSet
}

// MARK: - Convenience getter and setter for complex values/relations stored in database
extension User {
  var sendMessages: NSMutableSet {
    return mutableSetValueForKey(UserConstants.RawFields.SendMessages)
  }
  
  var divisions: NSMutableSet {
    return mutableSetValueForKey(UserConstants.RawFields.Divisions)
  }
  
  var administratedDivisions: NSMutableSet {
    return mutableSetValueForKey(UserConstants.RawFields.AdministratedDivisions)
  }
  
  var gender: Gender? {
    get {
      if rawGender != nil {
        return Gender(rawValue: rawGender!)
      } else {
        return nil
      }
    }
    set {
      rawGender = newValue?.rawValue
    }
  }
  
  var membershipStatus: MembershipStatus? {
    get {
      if rawMembershipStatus != nil {
        return MembershipStatus(rawValue: rawMembershipStatus!)
      } else {
        return nil
      }
    }
    set {
      rawMembershipStatus = newValue?.rawValue
    }
  }
  
  var avatar: UIImage? {
    get {
      if let imageData = rawAvatar {
        return UIImage(data: imageData)
      } else {
        return nil
      }
    }
    set {
      if let image = newValue {
        rawAvatar = UIImagePNGRepresentation(image)
      } else {
        rawAvatar = nil
      }
    }
  }
  
  var displayName: String {
    if let firstName = firstName, lastName = lastName {
      return "\(firstName) \(lastName)"
    } else {
      sync()
      return id
    }
  }
  
  var initials: String {
    if let firstName = firstName, lastName = lastName {
      return "\(firstName[firstName.startIndex])\(lastName[lastName.startIndex])"
    } else {
      sync()
      return "N/A"
    }
  }
}

// MARK: - MVCoreDataObject protocol functions
extension User: CoreDataObject {
  static var remoteId: String { return UserConstants.RemoteUser.Id }
  static var className: String { return UserConstants.ClassName }
  
  var syncRequired: Bool {
    return (email == nil || firstName == nil || lastName == nil)
  }
  
  func sync() {
    MVNetworkingHelper.syncUser(id)
  }
}

// MARK: - Printable protocol function
extension User: Printable {
  override var description: String {
    if let firstName = firstName, lastName = lastName, email = email {
      return "User \(firstName) \(lastName) [\(email)]"
    } else {
      return "User \(id)"
    }
  }
}

// MARK: - User object related enumerations
enum Gender: String {
  case Male = "MALE"
  case Female = "FEMALE"
}

enum MembershipStatus: String {
  case Active = "ACTIVE"
  case Passive = "PASSIVE"
  case Resigned = "RESIGNED"
}

// MARK: - User object related constants
struct UserConstants {
  static let ClassName = "User"
  
  // This struct defines the names of all database columns
  struct Fields {
    static let Id = "id"
    static let FirstName = "firstName"
    static let LastName = "lastName"
  }
  
  // This struct defines the names of all database columns/relations that should not be accessed directly
  struct RawFields {
    static let SendMessages = "rawSendMessages"
    static let Divisions = "rawDivisions"
    static let AdministratedDivisions = "rawAdministratedDivisions"
    static let GoingEvents = "rawGoingEvents"
    static let MaybeEvents = "rawMaybeEvents"
    static let DeclinedEvents = "rawDeclinedEvents"
    static let PendingEvents = "rawPendingEvents"
  }
  
  // This struct defines the names of the member fields on the remote object. They are used to parse the event.
  struct RemoteUser {
    static let Id = "id"
    static let FirstName = "firstName"
    static let LastName = "lastName"
    static let Email = "email"
    static let Birthday = "birthday"
    static let Gender = "gender"
    static let MembershipStatus = "membershipStatus"
    static let Street = "street"
    static let StreetNumber = "streetNumber"
    static let ZipCode = "zipCode"
    static let City = "city"
    static let Country = "country"
    static let Divisions = "divisions"
  }
}
