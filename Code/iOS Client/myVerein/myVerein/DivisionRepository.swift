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
        }
    }
    
    private let logger = XCGLogger.defaultInstance()

    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!

    /// This function returns the division defined through the response object. The object needs to be a dictionary, containing the id of the division. The division is either retrieved from the database or created and synced.
    func getOrCreateDivisionFrom(#serverResponseObject: Dictionary<String, AnyObject>) -> (division: Division?, error: NSError?) {
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
            logger.error("Unable to save database: \(error?.localizedDescription)")
        } else {
            logger.info("Successfully saved database")
        }
    }
}