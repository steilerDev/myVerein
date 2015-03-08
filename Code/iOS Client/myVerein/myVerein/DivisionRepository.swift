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

class DivisionRepository {
    
    private struct DivisionConstants {
        static let ClassName = "Division"
        static let IdField = "id"
        
        struct remoteDivision {
            static let Id = "id"
            static let Name = "name"
        }
    }

    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!

    /// This function returns the division defined through the response object. The object needs to be a dictionary, containing the id of the division. The division is either retrieved from the database or created and synced.
    func getDivision(requestObject: Dictionary<String, AnyObject>) -> (division: Division?, error: NSError?) {
        if let divisionId = requestObject[DivisionConstants.IdField] as? String {
            if let division = getDivision(divisionId) {
                return (division, nil)
            } else {
                return (createDivision(divisionId), nil)
            }
        } else {
            return (nil,
                MVError.createError(.MVDivisionCreationError,
                    failureReason: "Division could not be created, because the server response object could not be parsed",
                    underlyingError: .MVServerResponseParseError
                )
            )
        }
    }
    
    /// This function gathers the division object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
    func getDivision(id: String) -> Division? {
        // Create a new fetch request using the Message entity
        let fetchRequest = NSFetchRequest(entityName: DivisionConstants.ClassName)
        
        let predicate = NSPredicate(format: "\(DivisionConstants.IdField) == %@", id)
        fetchRequest.predicate = predicate
        
        // Execute the fetch request, and cast the results to an array of LogItem objects
        return managedObjectContext.executeFetchRequest(fetchRequest, error: nil)?.last as? Division
    }
    
    /// This function creates a new division using the id. After creation the function tries to fetch the remaining information asynchronsously.
    func createDivision(id: String) -> Division {
        let newItem = NSEntityDescription.insertNewObjectForEntityForName(DivisionConstants.ClassName, inManagedObjectContext: managedObjectContext) as! Division
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