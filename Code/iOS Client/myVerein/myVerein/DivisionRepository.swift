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
//  DivisionRepository.swift
//  This file holds all information related with parsing and retrieving a division.
//

import Foundation
import CoreData
import XCGLogger

class DivisionRepository: CoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  func findDivisionByUserMembershipStatus(userMembershipStatus: UserMembershipStatus) -> [Division]? {
    logger.verbose("Retrieving divisions the user's membership status is \(userMembershipStatus)")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: DivisionConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(DivisionConstants.RawFields.UserMembershipStatus) == %@", userMembershipStatus.rawValue)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)
  }
  
  func findAllDivisions() -> [Division]? {
    logger.verbose("Retrieving all divisions")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: DivisionConstants.ClassName)
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)
  }
  
  // MARK: - Population of division
  override func populateObject<T : CoreDataObject>(coreDataObject: T, usingDictionary dictionary: [String : AnyObject]) -> (T?, NSError?) {
    if let division = coreDataObject as? Division,
      name = dictionary[DivisionConstants.RemoteDivision.Name] as? String
    {
      if let adminUserDict = dictionary[DivisionConstants.RemoteDivision.AdminUser] as? [String: AnyObject] {
        logger.debug("Found an administrator for the division")
        let userRepository = UserRepository()
        let (adminUser: User?, error) = userRepository.getOrCreateUsingDictionary(adminUserDict, AndSync: true)
        // TODO: Or better?!
        if error != nil && adminUser == nil {
          logger.warning("Unable to parse admin user for division: \(error!.extendedDescription)")
          return (nil, error)
        } else {
          division.admin = adminUser!
        }
      } else {
        logger.debug("No admin user for division")
      }
      
      division.desc = dictionary[DivisionConstants.RemoteDivision.Description] as? String
      division.name = name
      division.lastSynced = NSDate()
      
      logger.info("Succesfully parsed and populaterd division")
      return ((division as! T), nil)
    } else {
      let error = MVError.createError(.MVServerResponseParseError)
      logger.error("Unable to parse division: \(error.extendedDescription)")
      return (nil, error)
    }
  }
}