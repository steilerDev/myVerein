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

// MARK: - Functions used to query the database
class EventRepository: CoreDataRepository {
  
  /// This function gathers all events, where the user responded with the provided event response
  func findEventsByUserResponse(userResponse: EventResponse) -> [Event]? {
    logger.verbose("Retrieving event with user response \(userResponse)")
    // Create a new fetch request using the event entity
    let fetchRequest = NSFetchRequest(entityName: EventConstants.ClassName)
    
    let predicate = NSPredicate(format: "\(EventConstants.RawFields.UserResponse) == %@", userResponse.rawValue)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)
  }
  
  /// This function returns the amount of times the user responded with a specific event response
  func countEventsWithUserResponse(userResponse: EventResponse) -> Int {
    logger.verbose("Checking how often the user responded with \(userResponse) on an event")
    return findEventsByUserResponse(userResponse)?.count ?? 0
  }
  
  /// This function returns the amount of events the user did not respond to
  func countPendingEvents() -> Int {
    return countEventsWithUserResponse(.Pending)
  }

  /// This function gathers all events that take place on the date or span over the date. The specified user responses are used for further filtering of the selection, because only events that have one of the specified user responses are considered. Pass nil
  ///
  /// :param: date The date should specify the moment on the start of the day. The function checks the database for event.startDate <= date && event.endDate >= date + 24h
  /// :param: andUserResponses Only events that have one of the specified user responses are returned. Pass nil if you don't want to filter according to the responses.
  func findEventsByDate(startDate: NSDate, andUserResponses userResponses: [EventResponse]?) -> [Event]? {
    logger.verbose("Retrieving all events on \(startDate) from database with user responses \(userResponses)")
    
    let fetchRequest = NSFetchRequest(entityName: EventConstants.ClassName)
    
    let endDate = startDate.dateByAddingTimeInterval(86400)
    
    // There are three cases how an event could span over the date, either it is completly during the day (end and start between day boundaries), or starts before the start of the day and ends after the start of the day, or starts before the end of the day and ends after the end of the day.
    
    let startAndEndBetweenBoundariesPredicate = NSCompoundPredicate.andPredicateWithSubpredicates([
      NSPredicate(format: "\(EventConstants.Fields.StartDate) >= %@ && \(EventConstants.Fields.StartDate) <= %@", startDate, endDate),
      NSPredicate(format: "\(EventConstants.Fields.EndDate) >= %@ && \(EventConstants.Fields.EndDate) <= %@", startDate, endDate),
    ])
    
    let startBeforeAndEndAfterStartOfDatePredicate = NSCompoundPredicate.andPredicateWithSubpredicates([
      NSPredicate(format: "\(EventConstants.Fields.StartDate) < %@", startDate),
      NSPredicate(format: "\(EventConstants.Fields.EndDate) > %@", startDate)
    ])
    
    let startBeforeAndEndAfterEndOfDatePredicate = NSCompoundPredicate.andPredicateWithSubpredicates([
      NSPredicate(format: "\(EventConstants.Fields.StartDate) < %@", endDate),
      NSPredicate(format: "\(EventConstants.Fields.EndDate) > %@", endDate)
    ])
    
    let datePredicate = NSCompoundPredicate.orPredicateWithSubpredicates([startAndEndBetweenBoundariesPredicate, startBeforeAndEndAfterStartOfDatePredicate, startBeforeAndEndAfterEndOfDatePredicate])
    
    if let userResponses = userResponses where !userResponses.isEmpty {
      var userResponsePredicates = [NSPredicate]()
      for response in userResponses {
        userResponsePredicates.append(NSPredicate(format: "\(EventConstants.RawFields.UserResponse) == %@", response.rawValue))
      }
      
      let compoundUserResponsePredicate = NSCompoundPredicate.orPredicateWithSubpredicates(userResponsePredicates)
      
      fetchRequest.predicate = NSCompoundPredicate.andPredicateWithSubpredicates([datePredicate, compoundUserResponsePredicate])
    } else {
      fetchRequest.predicate = datePredicate
    }
    return executeListRequest(fetchRequest)
  }
  
  /// This function gathers all events that take place on the date or span over the date.
  ///
  /// :param: date The date should specify the moment on the start of the day. The function checks the database for event.startDate <= date && event.endDate >= date + 24h
  func findEventsByDate(startDate: NSDate) -> [Event]? {
    return findEventsByDate(startDate, andUserResponses: nil)
  }
  
  /// This function returns true if there is an event on the date or spans over the date, otherwise it return false.
  ///
  /// :param: date The date should specify the moment on the start of the day. The function checks the database for event.startDate <= date && event.endDate >= date + 24h
  func isEventOnDate(date: NSDate) -> Bool {
    return !(findEventsByDate(date)?.isEmpty ?? true)
  }
  
  /// This function returns true if there is an event on the date or spans over the date which has the user response 'pending', 'maybe' or 'going', otherwise it return false.
  ///
  /// :param: date The date should specify the moment on the start of the day. The function checks the database for event.startDate <= date && event.endDate >= date + 24h
  func isDisplayableEventOnDate(date: NSDate) -> Bool {
    return !(findEventsByDate(date, andUserResponses: [.Pending, .Maybe, .Going])?.isEmpty ?? true)
  }

  override func populateObject<T: CoreDataObject>(coreDataObject: T, usingDictionary dictionary: [String : AnyObject]) -> (T?, NSError?) {
    if let event = coreDataObject as? Event,
      name = dictionary[EventConstants.RemoteEvent.Name] as? String,
      startDateTime = MVDateParser.parseDateTime(dictionary[EventConstants.RemoteEvent.StartDateTime] as? [String: AnyObject]),
      endDateTime = MVDateParser.parseDateTime(dictionary[EventConstants.RemoteEvent.EndDateTime] as? [String: AnyObject])
    {
      //Parsing invited division
      if let invitedDivisionArray = dictionary[EventConstants.RemoteEvent.InvitedDivision] as? [AnyObject] {
        let divisionRepository = DivisionRepository(inContext: self.managedObjectContext)
        let (invitedDivision: [Division]?, error) = divisionRepository.getOrCreateUsingArray(invitedDivisionArray, AndSync: true)
        if error != nil || invitedDivision == nil {
          logger.severe("Unable to gather invited division: \(error!.extendedDescription)")
          return (nil, error)
        } else {
          event.invitedDivision.addObjectsFromArray(invitedDivision!)
        }
      }
      
      //Parsing optionals
      event.locationName = dictionary[EventConstants.RemoteEvent.Location.Name] as? String
      event.locationLat = dictionary[EventConstants.RemoteEvent.Location.Lat] as? Double
      event.locationLng = dictionary[EventConstants.RemoteEvent.Location.Lng] as? Double
      event.eventDescription = dictionary[EventConstants.RemoteEvent.Description] as? String
      
      if let userResponseString = dictionary[EventConstants.RemoteEvent.UserResponse] as? String,
        userResponse = EventResponse(rawValue: userResponseString)
      {
        event.response = userResponse
      } else {
        event.response = .Pending
      }
      
      //Parsing non-optionals
      event.name = name
      event.startDate = startDateTime
      event.endDate = endDateTime
      
      event.lastSynced = NSDate()
      event.syncInProgress = false
      
      logger.info("Succesfully parsed and populaterd event")
      return ((event as! T), nil)
    } else {
      let error = MVError.createError(.MVServerResponseParseError)
      logger.error("Unable to parse event: \(error.extendedDescription)")
      return (nil, error)
    }
  }
}