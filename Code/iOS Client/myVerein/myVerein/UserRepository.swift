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
//  UserRepository.swift
//  This file holds all information related with parsing and retrieving a user.
//

import Foundation
import CoreData
import XCGLogger
import SwiftyUserDefaults

class UserRepository: CoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  
  // MARK: - Creation and population of user
  override func populateObject<T : CoreDataObject>(coreDataObject: T, usingDictionary dictionary: [String : AnyObject]) -> (T?, NSError?) {
    if let user = coreDataObject as? User,
      firstName = dictionary[UserConstants.RemoteUser.FirstName] as? String,
      lastName = dictionary[UserConstants.RemoteUser.LastName] as? String,
      email = dictionary[UserConstants.RemoteUser.Email] as? String
    {
      user.firstName = firstName
      user.lastName = lastName
      user.email = email
      
      if let divisionArray = dictionary[UserConstants.RemoteUser.Divisions] as? [AnyObject] {
        let divisionRepository = DivisionRepository()
        let (divisions: [Division]?, error) = divisionRepository.getOrCreateFrom(serverResponseArray: divisionArray)
        
        if error != nil && divisions == nil {
          logger.warning("Unable to parse divisions for user: \(error!.extendedDescription)")
          return (nil, error)
        } else {
          user.divisions.addObjectsFromArray(divisions!)
        }
      } else {
        logger.debug("No user divisions defined")
      }
      
      if let genderString = dictionary[UserConstants.RemoteUser.Gender] as? String,
        gender = Gender(rawValue: genderString)
      {
        user.gender = gender
      }
      
      if let membershipStatusString = dictionary[UserConstants.RemoteUser.MembershipStatus] as? String,
        membershipStatus = MembershipStatus(rawValue: membershipStatusString)
      {
        user.membershipStatus = membershipStatus
      }
      
      user.birthday = MVDateParser.parseDate(dictionary[UserConstants.RemoteUser.Birthday] as? [String: AnyObject])
      user.street = dictionary[UserConstants.RemoteUser.Street] as? String
      user.streetNumber = dictionary[UserConstants.RemoteUser.StreetNumber] as? String
      user.zipCode = dictionary[UserConstants.RemoteUser.ZipCode] as? String
      user.city = dictionary[UserConstants.RemoteUser.City] as? String
      user.country = dictionary[UserConstants.RemoteUser.Country] as? String
      user.lastSynced = NSDate()
      
      logger.info("Succesfully parsed and populaterd user")
      return ((user as! T), nil)
    } else {
      let error = MVError.createError(.MVServerResponseParseError)
      logger.error("Unable to parse user: \(error.extendedDescription)")
      return (nil, error)
    }
  }
}

extension UserRepository {
  class func getCurrentUser() -> User? {
    XCGLogger.debug("Gathering current user")
    if let currentUserId = Defaults[MVUserDefaultsConstants.UserID].string {
      return UserRepository().findBy(id: currentUserId)
    } else {
      XCGLogger.warning("Unable to find current user")
      return nil
    }
  }
}