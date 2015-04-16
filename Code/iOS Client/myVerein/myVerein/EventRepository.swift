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
//  EventRepository.swift
//  This file holds all information related with parsing and retrieving an event.
//

import Foundation
import CoreData
import XCGLogger

class EventRepository: CoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  /// This function gathers the event object with the corresponding id from the database and returns it. The object is nil if the program was unable to find it.
  func findEventBy(#id: String) -> Event? {
    logger.verbose("Retrieving event with ID \(id) from database")
    // Create a new fetch request using the event entity
    let fetchRequest = NSFetchRequest(entityName: EventConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(EventConstants.Fields.Id) == %@", id)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeSingleRequest(fetchRequest)
  }
  
  /// This function gathers all events, where the user responded with the provided event response
  func findEventsBy(#userResponse: EventResponse) -> [Event]? {
    logger.verbose("Retrieving event with user response \(userResponse)")
    // Create a new fetch request using the event entity
    let fetchRequest = NSFetchRequest(entityName: EventConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(EventConstants.RawFields.UserResponse) == %@", userResponse.rawValue)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)
  }
  
  /// This function returns the amount of times the user responded with a specific event response
  func countEventsWith(userResponse: EventResponse) -> Int {
    logger.verbose("Checking how often the user responded with \(userResponse) on an event")
    return findEventsBy(userResponse: userResponse)?.count ?? 0
  }
  
  /// This function returns the amount of events the user did not respond to
  func countPendingEvents() -> Int {
    return countEventsWith(.Pending)
  }

  /// This function gathers all events that take place on the date or span over the date.
  ///
  /// :param: date The date should specify the moment on the start of the day. The function checks the database for event.startDate <= date && event.endDate >= date + 24h
  func findEventsBy(date startDate: NSDate) -> [Event]? {
    logger.verbose("Retrieving all events on \(startDate) from database")
    
    let fetchRequest = NSFetchRequest(entityName: EventConstants.ClassName)
    
    let endDate = startDate.dateByAddingTimeInterval(86400)
    
    // There are three cases how an event could span over the date, either it is completly during the day (end and start between day boundaries), or starts before the start of the day and ends after the start of the day, or starts before the end of the day and ends after the end of the day.
    
    let startAndEndBetweenBoundariesPredicate = NSCompoundPredicate(type: .AndPredicateType,
      subpredicates: [
        NSPredicate(format: "\(EventConstants.Fields.StartDate) >= %@ && \(EventConstants.Fields.StartDate) <= %@", startDate, endDate),
        NSPredicate(format: "\(EventConstants.Fields.EndDate) >= %@ && \(EventConstants.Fields.EndDate) <= %@", startDate, endDate),
      ]
    )
    
    let startBeforeAndEndAfterStartOfDatePredicate = NSCompoundPredicate(type: .AndPredicateType,
      subpredicates: [
        NSPredicate(format: "\(EventConstants.Fields.StartDate) < %@", startDate),
        NSPredicate(format: "\(EventConstants.Fields.EndDate) > %@", startDate)
      ]
    )
    
    let startBeforeAndEndAfterEndOfDatePredicate = NSCompoundPredicate(type: .AndPredicateType,
      subpredicates: [
        NSPredicate(format: "\(EventConstants.Fields.StartDate) < %@", endDate),
        NSPredicate(format: "\(EventConstants.Fields.EndDate) > %@", endDate)
      ]
    )
    
    fetchRequest.predicate = NSCompoundPredicate(type: .OrPredicateType, subpredicates: [startAndEndBetweenBoundariesPredicate, startBeforeAndEndAfterStartOfDatePredicate, startBeforeAndEndAfterEndOfDatePredicate])
    
    return executeListRequest(fetchRequest)
  }
  
  /// This function returns true if there is an event on the date or spans over the date, otherwise it return false.
  ///
  /// :param: date The date should specify the moment on the start of the day. The function checks the database for event.startDate <= date && event.endDate >= date + 24h
  func isEventOn(#date: NSDate) -> Bool {
    logger.verbose("Checking if there is an event on \(date)")
    if let eventsOnDate = findEventsBy(date: date) where !eventsOnDate.isEmpty {
      logger.debug("Found events spanning over \(date): \(eventsOnDate)")
      return true
    } else {
      logger.debug("There is no event on \(date)")
      return false
    }
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
        logger.error("Unable to parse event: \(error.extendedDescription)")
        return (nil, error)
      }
    }
    logger.info("Returning \(newEvents.count) new events")
    return (newEvents, nil)
  }
  
  /// This function returns the event defined through the response object. The object needs to be an array, containing the id of the event. If the event does not exist, he is created and populated asynchronously.
  func getOrCreateEventFrom(#serverResponseObject: [String: AnyObject]) -> (event: Event?, error: NSError?) {
    logger.verbose("Retrieving event object from response object \(serverResponseObject)")
    if let eventId = serverResponseObject[EventConstants.Fields.Id] as? String {
      return getOrCreateEventFrom(id: eventId)
    } else {
      let error = MVError.createError(.MVUserCreationError,
        failureReason: "Event could not be created, because the server response object could not be parsed",
        underlyingError: .MVServerResponseParseError
      )
      logger.warning("Unable to create event from request object \(serverResponseObject): \(error.extendedDescription)")
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
      logger.severe("Unable to get existing event object: \(error!.extendedDescription)")
      return (nil, error)
    } else {
      logger.debug("Parsing event properties")
      if let event = event,
        name = serverResponseObject[EventConstants.RemoteEvent.Name] as? String,
        startDateTime = MVDateParser.parseDateTime(serverResponseObject[EventConstants.RemoteEvent.StartDateTime] as? [String: AnyObject]),
        endDateTime = MVDateParser.parseDateTime(serverResponseObject[EventConstants.RemoteEvent.EndDateTime] as? [String: AnyObject])
      {
        //Parsing invited division
        if let invitedDivisionArray = serverResponseObject[EventConstants.RemoteEvent.InvitedDivision] as? [AnyObject] {
          let divisionRepository = DivisionRepository()
          let (invitedDivision, error) = divisionRepository.getOrCreateDivisionsFrom(serverResponseObject: invitedDivisionArray)
          if error != nil && invitedDivision == nil {
            logger.severe("Unable to gather invited division: \(error!.extendedDescription)")
            return (nil, error)
          } else {
            event.invitedDivision.addObjectsFromArray(invitedDivision!)
          }
        }
        
        //Parsing optionals
        event.locationName = serverResponseObject[EventConstants.RemoteEvent.Location.Name] as? String
        event.locationLat = serverResponseObject[EventConstants.RemoteEvent.Location.Lat] as? Double
        event.locationLng = serverResponseObject[EventConstants.RemoteEvent.Location.Lng] as? Double
        event.eventDescription = serverResponseObject[EventConstants.RemoteEvent.Description] as? String
        
        if let userResponseString = serverResponseObject[EventConstants.RemoteEvent.UserResponse] as? String,
          userResponse = EventResponse(rawValue: userResponseString)
        {
          event.response = userResponse
        }
        
        //Parsing non-optionals
        event.name = name
        event.startDate = startDateTime
        event.endDate = endDateTime
        
        logger.info("Succesfully parsed and populaterd event")
        return (event, nil)
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse event: \(error.extendedDescription)")
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