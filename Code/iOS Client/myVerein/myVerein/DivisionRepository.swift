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

class DivisionRepository: MVCoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  /// This function gathers the division object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
  func findDivisionBy(#id: String) -> Division? {
    logger.verbose("Retrieving division with id \(id) from database")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: DivisionConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(DivisionConstants.Fields.Id) == %@", id)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeSingleRequest(fetchRequest)
  }
  
  func findDivisionBy(#userMembershipStatus: UserMembershipStatus) -> [Division]? {
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
  
  // MARK: - Creation and population of division
  
  /// This function returns the division defined through the response object. The object needs to be a dictionary, containing the id of the division. If the division does not exist, it is created and populated asynchronously.
  func getOrCreateDivisionFrom(#serverResponseObject: [String: AnyObject]) -> (division: Division?, error: NSError?) {
    logger.verbose("Retrieving division from response object \(serverResponseObject)")
    if let divisionId = serverResponseObject[DivisionConstants.Fields.Id] as? String {
      if let division = findDivisionBy(id: divisionId) {
        logger.info("Returning division with ID \(divisionId) from local database")
        return (division, nil)
      } else {
        logger.info("Creating new division with ID \(divisionId)")
        return (createDivision(divisionId), nil)
      }
    } else {
      let error = MVError.createError(.MVDivisionCreationError,
        failureReason: "Division could not be created, because the server response object could not be parsed",
        underlyingError: .MVServerResponseParseError
      )
      logger.error("Unable to retrieve division object: \(error.extendedDescription)")
      return (nil, error)
    }
  }
  
  /// This function returns a list of divisions defined through the server response array. If the divisions do not exist, they are created and populated asynchronously.
  func getOrCreateDivisionsFrom(#serverResponseObject: [AnyObject]) -> ([Division]?, NSError?) {
    logger.verbose("Parsing division from response object: \(serverResponseObject)")
    var searchedDivisions = [Division]()
    for division in serverResponseObject {
      if let divisionDict = division as? [String: AnyObject] {
        let (newDivision, error) = getOrCreateDivisionFrom(serverResponseObject: divisionDict)
        if error != nil && newDivision == nil {
          return (nil, error)
        } else {
          searchedDivisions.append(newDivision!)
        }
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse division: \(error.extendedDescription)")
        return (nil, error)
      }
    }
    logger.info("Returning \(searchedDivisions.count) new divisions")
    return (searchedDivisions, nil)
  }
  
  /// This function either populates an existing division using the response object, or creates a new division from the scratch
  func syncDivisionWith(#serverResponseObject: [String: AnyObject]) -> (Division?, NSError?) {
    logger.verbose("Creating division from response object \(serverResponseObject)")
    
    // TODO: Improve, because if division is created he gets populated again.
    let (division, error) = getOrCreateDivisionFrom(serverResponseObject: serverResponseObject)
    
    if division == nil && error != nil {
      logger.debug("Unable to get existing dvision object \(error!.extendedDescription)")
      return (nil, error)
    } else {
      logger.debug("Parsing division properties")
      if let division = division,
        name = serverResponseObject[DivisionConstants.RemoteDivision.Name] as? String
      {
        if let adminUserDict = serverResponseObject[DivisionConstants.RemoteDivision.AdminUser] as? [String: AnyObject] {
          logger.debug("Found an administrator for the division")
          let userRepository = UserRepository()
          let (adminUser, error) = userRepository.getOrCreateUserFrom(serverResponseObject: adminUserDict)
          
          if error != nil {
            logger.warning("Unable to parse admin user for division: \(error!.extendedDescription)")
            return (nil, error)
          } else {
            division.admin = adminUser
          }
        } else {
          logger.debug("No admin user for division")
        }
        
        division.desc = serverResponseObject[DivisionConstants.RemoteDivision.Description] as? String
        division.name = name
        division.lastSynced = NSDate()
        
        logger.info("Succesfully parsed and populaterd division")
        return (division, nil)
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse user: \(error.extendedDescription)")
        return (nil, error)
      }
    }
  }
  
  /// This function creates a new division using the id. After creation the function tries to fetch the remaining information asynchronsously.
  func createDivision(id: String) -> Division {
    logger.verbose("Creating new division with id \(id)")
    let newItem = NSEntityDescription.insertNewObjectForEntityForName(DivisionConstants.ClassName, inManagedObjectContext: managedObjectContext) as! Division
    newItem.id = id
    newItem.lastSynced = NSDate()
    /// Getting the rest of the division asynchronously
    newItem.sync()
    
    return newItem
  }
}