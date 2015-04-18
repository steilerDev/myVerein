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

/// This class is a general core data repository holding definitions for the logger, managed context and saving mechanics. On top of that it provides a general mechanism of parsing and creating objects retrieved from the server. Since a lot of these functions are using generics the error 'Cannot invode '...' with an argument list of type '...'' might appear. In this case explicit stating the type should resolve the problem.
class CoreDataRepository {
  
  let logger = XCGLogger.defaultInstance()
  
  // Retrieve the shared managedObjectContext from AppDelegate
  let managedObjectContext: NSManagedObjectContext
  
  // MARK: - Initializer
  
  /// This initializer initiates the repository using the default managed object context provided by the app delegate for executions on the main queue.
  init() {
    managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
  }
  
  /// This initializer initiates the repository using a managed object context for executions on any queue.
  init(inContext managedObjectContext: NSManagedObjectContext) {
    self.managedObjectContext = managedObjectContext
  }
  
  
  // MARK: - Synchronization of objects (Put here because you currently cannot overwrite a function that is declared in an extension
  
  /// This function is used to synchronize the object using the server response. If applicable all key/value pairs of the dictionary are assigned to the object. If the object, identified by it's id, does not exist yet, it gets created.
  ///
  /// :param: serverResponseDictionary A dictionary, where the key is defined by the core data's object variable 'remoteId' and the (string) value is the object's ID. Besides the id, the other values are used to populate the item.
  /// :returns: A core data object or nil if an error occured. In generel the provided error should then help identify the problem.
  func syncObjectUsingDictionary<T: CoreDataObject>(dictionary: [String: AnyObject]) -> (T?, NSError?) {
    logger.verbose("Creating \(T.className) from response dictionary \(dictionary)")
    
    let (object: T?, error) = getOrCreateUsingDictionary(dictionary, AndSync: false)
    
    if object == nil || error != nil {
      logger.debug("Unable to get existing \(T.className): \(error!.extendedDescription)")
      return (nil, error)
    } else {
      logger.debug("Parsing \(T.className) properties")
      return populateObject(object!, usingDictionary: dictionary)
    }
  }
  
  /// This function is using the provided dictionary to populate the core data object. This function can be seen as an abstract function, because it does not have any functionality and should be overwritten in CoreDataRepository's subclasses.
  ///
  /// :param: coreDataObject The object that is going to be populated.
  /// :param: dictionary The data used to populate the object, formatted as dictionary. The key/value pairs depend on the actual implementation.
  /// :returns: A core data object or nil if an error occured. In generel the provided error should then help identify the problem.
  func populateObject<T: CoreDataObject>(coreDataObject: T, usingDictionary dictionary: [String: AnyObject]) -> (T?, NSError?) {
    let error = MVError.createError(.MVFunctionNotImplemented)
    logger.severe("This function needs to be overwritten, because it does not have any functionality in it's base class: \(error.extendedDescription)")
    logger.debugExec { abort() }
    return (nil, error)
  }
}

// MARK: - Persistent function and variables (Saving context and checking if context changed)
extension CoreDataRepository {
  /// This variable tells an application if the database changed.
  var databaseDidChange: Bool {
    return managedObjectContext.hasChanges
  }
  
  // This function saves the database permanently, if needed.
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
}

// MARK: - Low level query functions
extension CoreDataRepository {
  /// This function executes a fetch request on the database, which is only expecting a single entry to be returned. If the object is out of sync it gets syncronized asynchrounously.
  ///
  /// :param: fetchRequest The fetch request that should be executed
  /// :returns: A single object conforming the query or nil if the query was unsuccessful. If there is more than one object returned by the query, the function returns the last item of the list.
  func executeSingleRequest<T: CoreDataObject>(fetchRequest: NSFetchRequest, AndSync sync: Bool = true) -> T? {
    return executeListRequest(fetchRequest)?.last
  }
  
  /// This function executes a fetch request on the database, which is expecting several entries to be returned. If the object is out of sync it gets syncronized asynchrounously.
  ///
  /// :param: fetchRequest The fetch request that should be executed
  /// :returns: A list of objects conforming the query or nil if the query was unsuccessful.
  func executeListRequest<T: CoreDataObject>(fetchRequest: NSFetchRequest, AndSync sync: Bool = true) -> [T]? {
    var error: NSError?
    if let queriedObject = managedObjectContext.executeFetchRequest(fetchRequest, error: &error) as? [T] {
      
      if sync {
      // This block checks each of the objects if they are in sync using their syncRequired property. If they are out of sync, the object is asynchrounously synchronized.
        {
          (backgroundContext:NSManagedObjectContext) in
          var error: NSError?
          if let backgroundObjects = backgroundContext.executeFetchRequest(fetchRequest, error: &error) as? [T] {
            for object in backgroundObjects.filter({$0.syncRequired}) {
              XCGLogger.debug("Re-syncing \(T.className) \(object)")
              object.sync()
            }
          } else {
            XCGLogger.error("Unable to check sync status of \(T.className): \(error?.extendedDescription)")
          }
        }~>
      }
      
      return queriedObject
    } else {
      logger.error("Unable to execute fetch request for \(T.className): \(error?.extendedDescription)")
      return nil
    }
  }
}

// MARK: Object creation and retrieve functions
extension CoreDataRepository {
  
  /// This function returns a list of core data objects defined through the server response object. If the object was allready in the database, it's synchronisation status is checked and -if needed- an asynchronous synchronization is performed, otherwise a new object is created and populated asynchronously.
  ///
  /// :param: serverResponseArray An array, which is either a pure list of object IDs (as string) or a dictionary, where the key is defined by the core data's object variable 'remoteId' and the (string) value is the object's ID. Besides the id, other values may be part of the response object. They are going to be ignored in case of creation.
  /// :param: sync Tells the system if it should synchronize the object after retrieval (if needed). Note: If this flag is not set the object does not event get synced after creation.
  /// :returns: A list of core data objects or nil if an error occured. In generel the provided error should then help identify the problem.
  func getOrCreateUsingArray<T: CoreDataObject>(array: [AnyObject], AndSync sync: Bool = true) -> ([T]?, NSError?) {
    logger.verbose("Parsing and retrieving \(T.className) from response array: \(array) (Sync after: \(sync))")
    var objectList = [T]()
    
    if let array = array as? [[String: AnyObject]] {
      logger.debug("The response object is a list of dictionaries: \(array)")
      for object in array {
        let (newObject:T?, error) = getOrCreateUsingDictionary(object, AndSync: sync)
        if error != nil && newObject == nil {
          return (nil, error)
        } else {
          objectList.append(newObject!)
        }
      }
    } else if let array = array as? [String] {
      logger.debug("The response object is a list of id's: \(array)")
      for object in array {
        let (newObject:T?, error) = getOrCreateUsingId(object, AndSync: sync)
        if error != nil && newObject == nil {
          return (nil, error)
        } else {
          objectList.append(newObject!)
        }
      }
    } else {
      let error = MVError.createError(.MVEntityCreationError,
        failureReason: "\(T.className) could not be created, because the server response object could not be parsed",
        underlyingError: .MVServerResponseParseError
      )
      logger.warning("Unable to create \(T.className) from request object \(array): \(error.extendedDescription)")
    }
    logger.info("Returning \(objectList.count) new \(T.className)")
    return (objectList, nil)
  }
  
  /// This function returns a core data object defined through the server response object. If the object was allready in the database, it's synchronisation status is checked and -if needed- an asynchronous synchronization is performed, otherwise a new object is created and populated asynchronously.
  ///
  /// :param: dictionary A dictionary, where the key is defined by the core data's object variable 'remoteId' and the (string) value is the object's ID. Besides the id, other values may be part of the object. They are going to be ignored in case of creation.
  /// :param: sync Tells the system if it should synchronize the object after retrieval (if needed). Note: If this flag is not set the object does not event get synced after creation.
  /// :returns: A core data object or nil if an error occured. In generel the provided error should then help identify the problem.
  func getOrCreateUsingDictionary<T: CoreDataObject>(dictionary: [String: AnyObject], AndSync sync: Bool = true) -> (T?, NSError?) {
    logger.verbose("Parsing and retrieving \(T.className) from response dictionary: \(dictionary) (Sync after: \(sync))")
    if let objectId = dictionary["id"] as? String {
      return getOrCreateUsingId(objectId, AndSync: sync)
    } else {
      let error = MVError.createError(.MVEntityCreationError,
        failureReason: "\(T.className) could not be created, because the server response object could not be parsed",
        underlyingError: .MVServerResponseParseError
      )
      logger.warning("Unable to create \(T.className) from request object \(dictionary): \(error.extendedDescription)")
      return (nil, error)
    }
  }
  
  /// This function returns a core data object defined through it's id. If the object was allready in the database, it's synchronisation status is checked and -if needed- an asynchronous synchronization is performed, otherwise a new object is created and populated asynchronously.
  ///
  /// :param: id The id representation of the object.
  /// :param: sync Tells the system if it should synchronize the object after retrieval (if needed). Note: If this flag is not set the object does not event get synced after creation.
  /// :returns: A core data object or nil if an error occured. In generel the provided error should then help identify the problem.
  func getOrCreateUsingId<T: CoreDataObject>(id: String, AndSync sync: Bool = true) -> (T?, NSError?) {
    logger.debug("Get or create \(T.className) from id \(id) (Sync after: \(sync))")
    if let object: T = findById(id, AndSync: sync) {
      if sync && object.syncRequired {
        object.sync()
      }
      logger.info("Returning \(T.className) with ID \(id) from local database")
      return (object, nil)
    } else {
      logger.info("Creating new \(T.className) with ID \(id)")
      let object: T = createObjectWithId(id, AndSync: sync)
      return (object, nil)
    }
  }
  
  /// This function creates a new core data object and synchronizes it asynchronously.
  ///
  /// :param: id The unique identifier of the object.
  /// :param: sync Tells the system if it should synchronize the object. Note: If this flag is not set the object does not event get synced after creation.
  /// :returns: A core data object, which is not fully synchronized, but should be at a later point in time (if the sync flag is set).
  func createObjectWithId<T: CoreDataObject>(id: String, AndSync sync: Bool = true) -> T {
    logger.verbose("Creating new \(T.className) with id \(id) (Sync after: \(sync))")
    let newItem = NSEntityDescription.insertNewObjectForEntityForName(T.className, inManagedObjectContext: managedObjectContext) as! T
    newItem.id = id
    
    // Getting rest of the object asynchronously
    if sync {
      newItem.sync()
    }
    
    return newItem
  }
  
  func findById<T: CoreDataObject>(id: String, AndSync sync: Bool = true) -> T? {
    // Create a new fetch request
    let fetchRequest = NSFetchRequest(entityName: T.className)
    
    let predicate = NSPredicate(format: "id == %@", id)
    fetchRequest.predicate = predicate
    
    return executeSingleRequest(fetchRequest)
  }
}

/// This protocol defines a core data object used within this application
protocol CoreDataObject: AnyObject {
  
  /// This static variable is returning the string, used to represent the unique identifier of the object on the server
  static var remoteId: String { get }
  
  /// This static variable is returning the class name of the object, that can be used to query this kind of object
  static var className: String { get }
  
  /// This variable is the unique identifier of the object within the application. It is expected to be @NSManaged, since the queries will use the string 'id' to identify objects.
  var id: String { get set }
  
  /// This computed variable should check if an object is out of sync, and return true if the object needs to be synchronized. This is check is executed, everytime the object is retrieved from the database. Note: This function needs to be executed on the same queue as the one used to gather the object, which is most likely the main queue and should therefore not be too expensive.
  var syncRequired: Bool { get }
  
  /// This variable tells the system if the current object is allready syncing. If this is the case the system should not start another sync action.
  var syncInProgress: Bool { get set }
  
  /// This function should execute the syncing of the object.
  func sync() -> ()
}