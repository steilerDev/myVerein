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
      
      // This block checks each of the objects if they are in sync using their syncRequired property. If they are out of sync, the object is asynchrounously synchronized.
      {
        (backgroundContext:NSManagedObjectContext) in
        var error: NSError?
        if let backgroundObjects = backgroundContext.executeFetchRequest(fetchRequest, error: &error) as? [T] {
          for object in backgroundObjects.filter({$0.syncRequired}) {
            XCGLogger.debug("Re-syncing object \(object)")
            object.sync()
          }
        } else {
          XCGLogger.error("Unable to check sync status: \(error?.localizedDescription)")
        }
        }~>
      
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
  var syncRequired: Bool { get }
  
  /// This function should execute the syncing of the object.
  func sync() -> ()
}