//
//  DivisionRepository.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData
import UIKit
import XCGLogger

class DivisionRepository {
    
    private struct DivisionConstants {
        static let ClassName = "Division"
        static let IdField = "id"
        
        struct remoteDivision {
            static let Id = "id"
            static let Name = "name"
            static let Description = "desc"
            static let AdminUser = "adminUser"
        }
    }
    
    private let logger = XCGLogger.defaultInstance()

    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!

    /// This function gathers the division object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
    func findDivisionBy(#id: String) -> Division? {
        logger.verbose("Retrieving division with id \(id) from database")
        // Create a new fetch request using the Message entity
        let fetchRequest = NSFetchRequest(entityName: DivisionConstants.ClassName)
        
        let predicate = NSPredicate(format: "\(DivisionConstants.IdField) == %@", id)
        fetchRequest.predicate = predicate
        
        // Execute the fetch request, and cast the results to an array of LogItem objects
        return managedObjectContext.executeFetchRequest(fetchRequest, error: nil)?.last as? Division
    }
    
    // MARK: - Creation and population of user
    
    /// This function returns the division defined through the response object. The object needs to be a dictionary, containing the id of the division. If the division does not exist, it is created and populated asynchronously.
    func getOrCreateDivisionFrom(#serverResponseObject: [String: AnyObject]) -> (division: Division?, error: NSError?) {
        logger.verbose("Retrieving division from response object \(serverResponseObject)")
        if let divisionId = serverResponseObject[DivisionConstants.IdField] as? String {
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
            logger.error("Unable to retrieve division object: \(error.localizedDescription)")
            return (nil, error)
        }
    }
    
    /// This function returns a list of divisions defined through the server response array. If the divisions do not exist, they are created and populated asynchronously.
    func getOrCreateDivisionsFrom(#serverResponseObject: [AnyObject]) -> ([Division]?, NSError?) {
        logger.verbose("Parsing division from response object: \(serverResponseObject)")
        var newDivisions = [Division]()
        for division in serverResponseObject {
            if let divisionDict = division as? [String: AnyObject] {
                let (newDivision, error) = getOrCreateDivisionFrom(serverResponseObject: divisionDict)
                if error != nil && newDivision == nil {
                    return (nil, error)
                } else {
                    newDivisions.append(newDivision!)
                }
            } else {
                let error = MVError.createError(.MVServerResponseParseError)
                logger.error("Unable to parse division: \(error.localizedDescription)")
                return (nil, error)
            }
        }
        logger.info("Returning \(newDivisions.count) new divisions")
        return (newDivisions, nil)
    }
    
    /// This function either populates an existing division using the response object, or creates a new division from the scratch
    func createDivisionFrom(#serverResponseObject: [String: AnyObject]) -> (Division?, NSError?) {
        logger.verbose("Creating division from response object \(serverResponseObject)")
        
        // TODO: Improve, because if user is created he gets populated again.
        let (wrappedDivision, error) = getOrCreateDivisionFrom(serverResponseObject: serverResponseObject)
        
        if wrappedDivision == nil && error != nil {
            logger.debug("Unable to get existing dvision object \(error?.localizedDescription)")
            return (nil, error)
        } else {
            logger.debug("Parsing division properties")
            if let division = wrappedDivision,
                name = serverResponseObject[DivisionConstants.remoteDivision.Name] as? String,
                description = serverResponseObject[DivisionConstants.remoteDivision.Description] as? String,
                adminUserDict = serverResponseObject[DivisionConstants.remoteDivision.AdminUser] as? [String: AnyObject]
            {
                let userRepository = UserRepository()
                let (adminUser, error) = userRepository.getOrCreateUserFrom(serverResponseObject: adminUserDict)
                
                if error != nil && adminUser == nil {
                    logger.warning("Unable to parse admin user for division: \(error?.localizedDescription)")
                    return (nil, error)
                } else {
                    logger.debug("Successfully parsed all properties")
                    
                    division.name = name
                    division.desc = description
                    division.setAdminUser(adminUser!)
                    
                    logger.info("Succesfully parsed and populaterd division")
                    return (division, nil)
                }
            } else {
                let error = MVError.createError(.MVServerResponseParseError)
                logger.error("Unable to parse user: \(error.localizedDescription)")
                return (nil, error)
            }
        }
    }
    
    /// This function creates a new division using the id. After creation the function tries to fetch the remaining information asynchronsously.
    func createDivision(id: String) -> Division {
        logger.verbose("Creating new division with id \(id)")
        let newItem = NSEntityDescription.insertNewObjectForEntityForName(DivisionConstants.ClassName, inManagedObjectContext: managedObjectContext) as! Division
        newItem.id = id
        
        /// TODO: Get rest of the object
        
        return newItem
    }
    
    /// This function saves the database permanently
    func save() {
        logger.verbose("Saving database changes")
        var error : NSError?
        if managedObjectContext.save(&error) {
            logger.info("Successfully saved database")
        } else {
            logger.error("Unable to save database: \(error?.localizedDescription)")
        }
    }
}