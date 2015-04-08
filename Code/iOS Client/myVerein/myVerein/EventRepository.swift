//
//  EventRepository.swift
//  myVerein
//
//  Created by Frank Steiler on 04/04/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData
import XCGLogger

class EventRepository: MVCoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  /// This function gathers the event object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
  private func findEventBy(#id: String) -> Event? {
    logger.verbose("Retrieving event with ID \(id) from database")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: EventConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(EventConstants.IdField) == %@", id)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeSingleRequest(fetchRequest)
  }
  
  
  // MARK: - Creation and population of event
  
  /// This function returns a list of user defined through the server response array. If the event do not exist, they are created and populated asynchronously.
  func getOrCreateEventsFrom(#serverResponseObject: [AnyObject]) -> ([Event]?, NSError?) {
    logger.verbose("Parsing events from response object: \(serverResponseObject)")
    var newEvents = [Event]()
    for event in serverResponseObject {
      if let eventDict = event as? [String: AnyObject] {
        let (newEvent, error) = getOrCreateEventFrom(serverResponseObject: eventDict)
        if error != nil && newEvent == nil {
          return (nil, error)
        } else {
          newEvents.append(newEvent!)
        }
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse event: \(error.localizedDescription)")
        return (nil, error)
      }
    }
    logger.info("Returning \(newEvents.count) new user")
    return (newEvents, nil)
  }
  
  /// This function returns the event defined through the response object. The object needs to be an array, containing the id of the event. If the event does not exist, he is created and populated asynchronously.
  func getOrCreateEventFrom(#serverResponseObject: [String: AnyObject]) -> (event: Event?, error: NSError?) {
    logger.verbose("Retrieving event object from response object \(serverResponseObject)")
    if let eventId = serverResponseObject[DivisionConstants.IdField] as? String {
      return getOrCreateEventFrom(id: eventId)
    } else {
      let error = MVError.createError(.MVUserCreationError,
        failureReason: "Event could not be created, because the server response object could not be parsed",
        underlyingError: .MVServerResponseParseError
      )
      logger.warning("Unable to create event from request object \(serverResponseObject): \(error.localizedDescription)")
      return (nil, error)
    }
  }
  
  /// This functino returns the event defined by its id.
  func getOrCreateEventFrom(#id: String) -> (event: Event?, error: NSError?) {
    logger.debug("Get or create event from id \(id)")
    if let user = findEventBy(id: id) {
      logger.info("Returning event with ID \(id) from local database")
      return (user, nil)
    } else {
      logger.info("Creating new event with ID \(id)")
      return (createEvent(id), nil)
    }
  }
  
  /// This function either populates an existing event using the response object, or creates a new event from the scratch
  func syncEventWith(#serverResponseObject: [String: AnyObject]) -> (event: Event?, error: NSError?) {
    logger.verbose("Creating event from response object \(serverResponseObject)")
    
    // TODO: Improve, because if user is created he gets populated again.
    let (event, error) = getOrCreateEventFrom(serverResponseObject: serverResponseObject)
    
    if event == nil && error != nil {
      logger.severe("Unable to get existing event object: \(error?.localizedDescription)")
      return (nil, error)
    } else {
      logger.debug("Parsing event properties")
      if let event = event,
        name = serverResponseObject[EventConstants.RemoteEvent.Name] as? String,
        startDateTime = MVDateParser.parseDate(serverResponseObject[EventConstants.RemoteEvent.StartDateTime] as? [String: AnyObject]),
        endDateTime = MVDateParser.parseDate(serverResponseObject[EventConstants.RemoteEvent.EndDateTime] as? [String: AnyObject])
      {
        if let invitedDivisionArray = serverResponseObject[EventConstants.RemoteEvent.InvitedDivision] as? [AnyObject] {
          let divisionRepository = DivisionRepository()
          let (invitedDivision, error) = divisionRepository.getOrCreateDivisionsFrom(serverResponseObject: invitedDivisionArray)
          if error != nil && invitedDivision == nil {
            logger.severe("Unable to gather invited division: \(error?.localizedDescription)")
            return (nil, error)
          } else {
            event.invited
          }
        }
        /// TODO: Parse event
        
        logger.info("Succesfully parsed and populaterd user")
        return (event, nil)
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse event: \(error.localizedDescription)")
        return (nil, error)
      }
    }
  }
  
  /// This function creates a new user using his id. After creation the function tries to fetch the remaining information asynchronously.
  func createEvent(id: String) -> Event {
    logger.verbose("Creating new event with id \(id)")
    let newItem = NSEntityDescription.insertNewObjectForEntityForName(EventConstants.ClassName, inManagedObjectContext: managedObjectContext) as! Event
    newItem.id = id
    
    // Getting rest of the event asynchronously
    newItem.sync()
    
    return newItem
  }
}