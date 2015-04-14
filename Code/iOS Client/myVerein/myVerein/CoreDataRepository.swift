//
// Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

//
//  CoreDataRepository.swift
//  This file creates the base class for all repositories interacting with the database, providing saving and retrieving functions, as well as defining a protocol for all objects strored in the database and handled by the repository classes.
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
  
  /// This function saves the database permanently, if needed.
  func save() {
    if managedObjectContext.hasChanges {
      logger.verbose("Saving database changes")
      var error : NSError?
      if managedObjectContext.save(&error) {
        logger.info("Successfully saved database")
      } else {
        logger.error("Unable to save database: \(error?.extendedDescription)")
      }
    } else {
      logger.info("No need to save the database")
    }
  }
  
  /// This function executes a fetch request on the database, which is only expecting a single entry to be returned.
  func executeSingleRequest<T: MVCoreDataObject>(fetchRequest: NSFetchRequest) -> T? {
    return executeListRequest(fetchRequest)?.last
  }
  
  /// This function executes a fetch request on the database, which is expecting several entries to be returned.
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
          XCGLogger.error("Unable to check sync status: \(error?.extendedDescription)")
        }
      }~>
      
      return queriedObject
    } else {
      logger.error("Unable to execute fetch request: \(error?.extendedDescription)")
      return nil
    }
  }
}

/// This protocol defines a core data object used within this application
protocol MVCoreDataObject: AnyObject {
  
  /// This computed variable should check if an object is out of sync, and return true if the object needs to be synchronized. This is check is executed, everytime the object is retrieved from the database. Note: This function needs to be executed on the same queue as the one used to gather the object, which is most likely the main queue and should therefore not be too expensive.
  var syncRequired: Bool { get }
  
  /// This function should execute the syncing of the object.
  func sync() -> ()
}