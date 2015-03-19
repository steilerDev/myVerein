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

class UserRepository: MVCoreDataRepository {
  
  struct UserConstants {
    static let ClassName = "User"
    static let IdField = "id"
    
    struct remoteUser {
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
  
  // MARK: - Functions used to query the database
  
  /// This function gathers the user object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
  func findUserBy(#id: String) -> User? {
    logger.verbose("Retrieving user with ID \(id) from database")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: UserConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(UserConstants.IdField) == %@", id)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
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
    if let userId = serverResponseObject[UserConstants.IdField] as? String {
      if let user = findUserBy(id: userId) {
        logger.info("Returning user with ID \(userId) from local database")
        return (user, nil)
      } else {
        logger.info("Creating new user with ID \(userId)")
        return (createUser(userId), nil)
      }
    } else {
      let error = MVError.createError(.MVUserCreationError,
        failureReason: "User could not be created, because the server response object could not be parsed",
        underlyingError: .MVServerResponseParseError
      )
      logger.warning("Unable to create user from request object \(serverResponseObject): \(error.localizedDescription)")
      return (nil, error)
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
        firstName = serverResponseObject[UserConstants.remoteUser.FirstName] as? String,
        lastName = serverResponseObject[UserConstants.remoteUser.LastName] as? String,
        email = serverResponseObject[UserConstants.remoteUser.Email] as? String
      {
        user.firstName = firstName
        user.lastName = lastName
        user.email = email
        
        if let divisionArray = serverResponseObject[UserConstants.remoteUser.Divisions] as? [AnyObject] {
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
        
        if let genderString = serverResponseObject[UserConstants.remoteUser.Gender] as? String,
          gender = User.Gender(rawValue: genderString)
        {
          user.gender = gender
        }
        
        if let membershipStatusString = serverResponseObject[UserConstants.remoteUser.MembershipStatus] as? String,
          membershipStatus = User.MembershipStatus(rawValue: membershipStatusString)
        {
          user.membershipStatus = membershipStatus
        }
        
        user.birthday = DateParser.parseDate(serverResponseObject[UserConstants.remoteUser.Birthday] as? [String: AnyObject])
        user.street = serverResponseObject[UserConstants.remoteUser.Street] as? String
        user.streetNumber = serverResponseObject[UserConstants.remoteUser.StreetNumber] as? String
        user.zipCode = serverResponseObject[UserConstants.remoteUser.ZipCode] as? String
        user.city = serverResponseObject[UserConstants.remoteUser.City] as? String
        user.country = serverResponseObject[UserConstants.remoteUser.Country] as? String
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
}