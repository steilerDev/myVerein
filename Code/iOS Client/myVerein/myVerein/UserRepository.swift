//
//  UserRepository.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import UIKit
import CoreData
import XCGLogger

class UserRepository {
    
    private struct UserConstants {
        static let ClassName = "User"
        static let IdField = "id"
        
        struct remoteDivision {
            static let Id = "id"
            static let FirstName = "firstName"
            static let LastName = "lastName"
            static let Email = "email"
        }
    }
    
    private let logger = XCGLogger.defaultInstance()
    
    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
    
    /// This function returns the user defined through the response object. The object needs to be an array, containing the id of the user. The user is either retrieved from the database or created and synced.
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
    
    /// This function gathers the user object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
    func findUserBy(#id: String) -> User? {
        logger.verbose("Retrieving user with ID \(id) from database")
        // Create a new fetch request using the Message entity
        let fetchRequest = NSFetchRequest(entityName: UserConstants.ClassName)
        
        let predicate = NSPredicate(format: "\(UserConstants.IdField) == %@", id)
        fetchRequest.predicate = predicate
        
        // Execute the fetch request, and cast the results to an array of LogItem objects
        return managedObjectContext.executeFetchRequest(fetchRequest, error: nil)?.last as? User
    }
    
    /// This function creates a new division using the id. After creation the function tries to fetch the remaining information asynchronsously.
    func createUser(id: String) -> User {
        logger.verbose("Creating new user with id \(id)")
        let newItem = NSEntityDescription.insertNewObjectForEntityForName(UserConstants.ClassName, inManagedObjectContext: managedObjectContext) as! User
        newItem.id = id
        
        // Getting rest of the user asynchronously
        MVNetworkingHelper.syncUser(id)
        
        return newItem
    }
    
    /// This function parses a user using an array provided by a server request
    func parseUserFrom(#serverResponseObject: [String: AnyObject]) -> (User?, NSError?) {
        logger.verbose("Creating user from response object \(serverResponseObject)")
        
        let (user, error) = getOrCreateUserFrom(serverResponseObject: serverResponseObject)
        
        if user == nil && error != nil {
            logger.debug("Unable to get existing user object \(error?.localizedDescription)")
            return (nil, error)
        } else {
            logger.debug("Populating user object")
            
            // TODO: Parse user object
            
        }
        return (nil, nil)
    }
    
    /// This function saves the database permanently
    func save() {
        logger.verbose("Saving database changes")
        var error : NSError?
        if managedObjectContext.save(&error) {
            logger.error("Unable to save database: \(error?.localizedDescription)")
        } else {
            logger.info("Successfully saved database")
        }
    }
}