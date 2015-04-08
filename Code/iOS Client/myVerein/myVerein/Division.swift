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
//  Division.swift
//  This file holds all information related to the division object of the application
//

import Foundation
import CoreData

// MARK: - Pure database object, holding only information stored in database
class Division: NSManagedObject {
  
  @NSManaged var id: String
  @NSManaged var desc: String?
  @NSManaged var name: String?
  @NSManaged var lastSynced: NSDate
  @NSManaged var latestMessage: Message?
  @NSManaged var admin: User?
  
  // Raw values stored in database, convenience getter and setter are provided in an extension. This values should -in general- not be accessed directly.
  @NSManaged var rawUserMembershipStatus: String
}

// MARK: - Convenience getter and setter for complex values stored in database
extension Division {
  var userMembershipStatus: UserMembershipStatus {
    get {
      return UserMembershipStatus(rawValue: rawUserMembershipStatus) ?? .NoMember
    }
    set {
      rawUserMembershipStatus = newValue.rawValue
    }
  }
}

// MARK: - MVCoreDataObject protocol functions
extension Division: MVCoreDataObject {
  var syncRequired: Bool {
    return name == nil
  }
  
  func sync() {
    MVNetworkingHelper.syncDivision(id)
  }
}

// MARK: - Enums
enum UserMembershipStatus: String {
  case Member = "MEMBER"
  case FormerMember = "FORMERMEMBER"
  case NoMember = "NOMEMBER"
}

// MARK: - Constants
struct DivisionConstants {
  static let ClassName = "Division"
  static let IdField = "id"
  static let UserMembershipStatus = "rawUserMembershipStatus"
  static let Name = "name"
  static let Messages = "rawChatMessage"
  static let LatestMessage = "latestMessage"
  static let ChatMessages = "rawChatMessage"
  static let EnrolledUser = "rawEnrolledUser"
  
  struct RemoteDivision {
    static let Id = "id"
    static let Name = "name"
    static let Description = "desc"
    static let AdminUser = "adminUser"
  }
}