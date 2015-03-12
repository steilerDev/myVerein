//
//  CoreDataRepository.swift
//  myVerein
//
//  Created by Frank Steiler on 11/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import XCGLogger
import UIKit
import CoreData

/// This class is a general core data repository holding definitions for the logger, managed context and saving mechanics.
class MVCoreDataRepository {
    
    let logger = XCGLogger.defaultInstance()
    
    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
    
    /// This function saves the database permanently
    func save() {
        if managedObjectContext.hasChanges {
            logger.verbose("Saving database changes")
            var error : NSError?
            if managedObjectContext.save(&error) {
                logger.info("Successfully saved database")
            } else {
                logger.error("Unable to save database: \(error?.localizedDescription)")
            }
        } else {
            logger.info("No need to save the database")
        }
    }
    
    func executeSingleRequest<T: MVCoreDataObject>(fetchRequest: NSFetchRequest) -> T? {
        return executeListRequest(fetchRequest)?.last
    }
    
    func executeListRequest<T: MVCoreDataObject>(fetchRequest: NSFetchRequest) -> [T]? {
        var error: NSError?
        if let queriedObject = managedObjectContext.executeFetchRequest(fetchRequest, error: &error) as? [T] {
            //Every division which was not synced so far needs to be synced. This statement is executed on a background thread, using the marshal operator
            for object in queriedObject.filter(T.syncRequired) { T.syncFunction(object) }
            return queriedObject
        } else {
            logger.error("Unable to execute fetch request: \(error?.localizedDescription)")
            return nil
        }
    }
}

/// This protocol defines a core data object used within this application
protocol MVCoreDataObject: AnyObject {
    
    /// This function should check an object, and return true if the object needs to be synchronized. This is check is executed, everytime the object is retrieved from the database. Note: This function needs to be executed on the same queue as the one used to gather the object, which is most likely the main queue.
    static var syncRequired: MVCoreDataObject -> Bool { get }
    
    /// This function should execute the syncing of the object.
    static var syncFunction: MVCoreDataObject -> () { get }
}