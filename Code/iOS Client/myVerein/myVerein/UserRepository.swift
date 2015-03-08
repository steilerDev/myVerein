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
    
    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
    
    /// This function returns the user defined through the response object. The object needs to be an array, containing the id of the user. The user is either retrieved from the database or created and synced.
    func getUser(requestObject: Dictionary<String, AnyObject>) -> (user: User?, error: NSError?) {
        if let userId = requestObject[UserConstants.IdField] as? String {
            if let user = getUser(userId) {
                return (user, nil)
            } else {
                return (createUser(userId), nil)
            }
        } else {
            return (nil,
                MVError.createError(.MVUserCreationError,
                    failureReason: "User could not be created, because the server response object could not be parsed",
                    underlyingError: .MVServerResponseParseError
                )
            )
        }
    }
    
    /// This function gathers the user object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
    func getUser(id: String) -> User? {
        // Create a new fetch request using the Message entity
        let fetchRequest = NSFetchRequest(entityName: UserConstants.ClassName)
        
        let predicate = NSPredicate(format: "\(UserConstants.IdField) == %@", id)
        fetchRequest.predicate = predicate
        
        // Execute the fetch request, and cast the results to an array of LogItem objects
        return managedObjectContext.executeFetchRequest(fetchRequest, error: nil)?.last as? User
    }
    
    /// This function creates a new division using the id. After creation the function tries to fetch the remaining information asynchronsously.
    func createUser(id: String) -> User {
        let newItem = NSEntityDescription.insertNewObjectForEntityForName(UserConstants.ClassName, inManagedObjectContext: managedObjectContext) as! User
        newItem.id = id
        
        /// TODO: Get rest of the object
        
        return newItem
    }
    
    /// This function saves the database permanently
    func save() {
        var error : NSError?
        if managedObjectContext.save(&error) {
            println(error?.localizedDescription)
        }
    }
}