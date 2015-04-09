//
//  UserRepository.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData
import XCGLogger
import SwiftyUserDefaults

class UserRepository: MVCoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  /// This function gathers the user object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
  private func findUserBy(#id: String) -> User? {
    logger.verbose("Retrieving user with ID \(id) from database")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: UserConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(UserConstants.Fields.Id) == %@", id)
    fetchRequest.predicate = predicate
    
    return executeSingleRequest(fetchRequest)
  }
  
  // MARK: - Creation and population of user
  
  /// This function returns a list of user defined through the server response array. If the user do not exist, they are created and populated asynchronously.
  func getOrCreateUsersFrom(#serverResponseObject: [AnyObject]) -> ([User]?, NSError?) {
    logger.verbose("Parsing user from response object: \(serverResponseObject)")
    var newUsers = [User]()
    for user in serverResponseObject {
      if let userDict = user as? [String: AnyObject] {
        let (newUser, error) = getOrCreateUserFrom(serverResponseObject: userDict)
        if error != nil && newUser == nil {
          return (nil, error)
        } else {
          newUsers.append(newUser!)
        }
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse user: \(error.localizedDescription)")
        return (nil, error)
      }
    }
    logger.info("Returning \(newUsers.count) new user")
    return (newUsers, nil)
  }
  
  /// This function returns the user defined through the response object. The object needs to be an array, containing the id of the user. If the user does not exist, he is created and populated asynchronously.
  func getOrCreateUserFrom(#serverResponseObject: [String: AnyObject]) -> (user: User?, error: NSError?) {
    logger.verbose("Retrieving user object from response object \(serverResponseObject)")
    if let userId = serverResponseObject[UserConstants.Fields.Id] as? String {
      return getOrCreateUserFrom(id: userId)
    } else {
      let error = MVError.createError(.MVUserCreationError,
        failureReason: "User could not be created, because the server response object could not be parsed",
        underlyingError: .MVServerResponseParseError
      )
      logger.warning("Unable to create user from request object \(serverResponseObject): \(error.localizedDescription)")
      return (nil, error)
    }
  }
  
  /// This functino returns the user defined by its id.
  func getOrCreateUserFrom(#id: String) -> (user: User?, error: NSError?) {
    logger.debug("Get or create user from id \(id)")
    if let user = findUserBy(id: id) {
      logger.info("Returning user with ID \(id) from local database")
      return (user, nil)
    } else {
      logger.info("Creating new user with ID \(id)")
      return (createUser(id), nil)
    }
  }
  
  /// This function either populates an existing user using the response object, or creates a new user from the scratch
  func syncUserWith(#serverResponseObject: [String: AnyObject]) -> (User?, NSError?) {
    logger.verbose("Creating user from response object \(serverResponseObject)")
    
    // TODO: Improve, because if user is created he gets populated again.
    let (wrappedUser, error) = getOrCreateUserFrom(serverResponseObject: serverResponseObject)
    
    if wrappedUser == nil && error != nil {
      logger.debug("Unable to get existing user object \(error?.localizedDescription)")
      return (nil, error)
    } else {
      logger.debug("Parsing user properties")
      if let user = wrappedUser,
        firstName = serverResponseObject[UserConstants.RemoteUser.FirstName] as? String,
        lastName = serverResponseObject[UserConstants.RemoteUser.LastName] as? String,
        email = serverResponseObject[UserConstants.RemoteUser.Email] as? String
      {
        user.firstName = firstName
        user.lastName = lastName
        user.email = email
        
        if let divisionArray = serverResponseObject[UserConstants.RemoteUser.Divisions] as? [AnyObject] {
          let divisionRepository = DivisionRepository()
          let (divisions, error) = divisionRepository.getOrCreateDivisionsFrom(serverResponseObject: divisionArray)
          
          if error != nil && divisions == nil {
            logger.warning("Unable to parse divisions for user: \(error?.localizedDescription)")
            return (nil, error)
          } else {
            for division in divisions! {
              user.divisions.addObject(division)
            }
          }
        } else {
          logger.debug("No user divisions defined")
        }
        
        if let genderString = serverResponseObject[UserConstants.RemoteUser.Gender] as? String,
          gender = Gender(rawValue: genderString)
        {
          user.gender = gender
        }
        
        if let membershipStatusString = serverResponseObject[UserConstants.RemoteUser.MembershipStatus] as? String,
          membershipStatus = MembershipStatus(rawValue: membershipStatusString)
        {
          user.membershipStatus = membershipStatus
        }
        
        user.birthday = MVDateParser.parseDate(serverResponseObject[UserConstants.RemoteUser.Birthday] as? [String: AnyObject])
        user.street = serverResponseObject[UserConstants.RemoteUser.Street] as? String
        user.streetNumber = serverResponseObject[UserConstants.RemoteUser.StreetNumber] as? String
        user.zipCode = serverResponseObject[UserConstants.RemoteUser.ZipCode] as? String
        user.city = serverResponseObject[UserConstants.RemoteUser.City] as? String
        user.country = serverResponseObject[UserConstants.RemoteUser.Country] as? String
        user.lastSynced = NSDate()
        
        logger.info("Succesfully parsed and populaterd user")
        return (user, nil)
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse user: \(error.localizedDescription)")
        return (nil, error)
      }
    }
  }
  
  /// This function creates a new user using his id. After creation the function tries to fetch the remaining information asynchronously.
  func createUser(id: String) -> User {
    logger.verbose("Creating new user with id \(id)")
    let newItem = NSEntityDescription.insertNewObjectForEntityForName(UserConstants.ClassName, inManagedObjectContext: managedObjectContext) as! User
    newItem.id = id
    newItem.lastSynced = NSDate()
    
    // Getting rest of the user asynchronously
    newItem.sync()
    
    return newItem
  }
  
  class func getCurrentUser() -> User? {
    XCGLogger.debug("Gathering current user")
    if let currentUserId = Defaults[MVUserDefaultsConstants.UserID].string {
      return UserRepository().findUserBy(id: currentUserId)
    } else {
      XCGLogger.warning("Unable to find current user")
      return nil
    }
  }
}