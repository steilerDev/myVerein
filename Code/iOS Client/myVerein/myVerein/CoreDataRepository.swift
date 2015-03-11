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

/// This class is a general core data repository holding definitions for the logger, managed context and saving mechanics.
class CoreDataRepository {
    
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
}