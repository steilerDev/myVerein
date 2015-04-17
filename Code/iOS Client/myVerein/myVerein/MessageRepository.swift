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
//  MessageRepository.swift
//  This file holds all information related with parsing and retrieving a message.
//

import Foundation
import CoreData
import XCGLogger

class MessageRepository: CoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  /// This function gathers all messages send through the division's chat
  func findMessagesByDivision(division: Division) -> [Message]? {
    logger.verbose("Getting all messages from database by division \(division.id)")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: MessageConstants.ClassName)
    fetchRequest.fetchBatchSize = MessageConstants.BatchSize
    let sortDescriptor = NSSortDescriptor(key: MessageConstants.Fields.Timestamp, ascending: true)
    fetchRequest.sortDescriptors = [sortDescriptor]
    
    let predicate = NSPredicate(format: "\(MessageConstants.Fields.Division) == %@", division)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)
  }
  
  /// This function gathers all messages, read or unread by the user and send through the division's chat
  func findMessagesByDivision(division: Division, andReadFlag readFlag: Bool) -> [Message]? {
    logger.verbose("Getting all messages from database by division \(division.id)")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: MessageConstants.ClassName)
    fetchRequest.fetchBatchSize = MessageConstants.BatchSize
    
    let sortDescriptor = NSSortDescriptor(key: MessageConstants.Fields.Timestamp, ascending: true)
    fetchRequest.sortDescriptors = [sortDescriptor]
    
    let predicate = NSCompoundPredicate(type: .AndPredicateType,
      subpredicates: [
        NSPredicate(format: "\(MessageConstants.Fields.Division) == %@", division),
        NSPredicate(format: "\(MessageConstants.Fields.Read) == %@", readFlag)
      ]
    )
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)
  }
  
  /// This function counts the unread messages of the user send through the division's chat
  func countUnreadMessagesInDivision(division: Division) -> Int {
    return findMessagesByDivision(division, andReadFlag: false)?.count ?? 0
  }
  
  /// This function gathers all messages, read or unread by the user.
  func findMessagesByReadFlag(readFlag: Bool) -> [Message]? {
    logger.verbose("Getting message with read flag set to \(readFlag)")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: MessageConstants.ClassName)
    fetchRequest.fetchBatchSize = MessageConstants.BatchSize
    
    let predicate = NSPredicate(format: "\(MessageConstants.Fields.Read) == %@", readFlag)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)
  }
  
  /// This function counts the unread messages of the user
  func countUnreadMessages() -> Int {
    return findMessagesByReadFlag(false)?.count ?? 0
  }
  
  // MARK: - Creation and population of message
  override func populateObject<T : CoreDataObject>(coreDataObject: T, usingDictionary dictionary: [String : AnyObject]) -> (T?, NSError?) {
    if let message = coreDataObject as? Message,
      id = dictionary[MessageConstants.RemoteMessage.Id] as? String,
      content = dictionary[MessageConstants.RemoteMessage.Content] as? String,
      timestampDict = dictionary[MessageConstants.RemoteMessage.Timestamp] as? [String: AnyObject],
      timestamp = MVDateParser.parseDateTime(timestampDict),
      divisionDict = dictionary[MessageConstants.RemoteMessage.Division] as? [String: AnyObject],
      senderDict = dictionary[MessageConstants.RemoteMessage.Sender] as? [String: AnyObject]
    {
      let divisionRepository = DivisionRepository()
      let userRepository = UserRepository()
      let (division: Division?, divisionError) = divisionRepository.getOrCreateUsingDictionary(divisionDict, AndSync: true)
      let (sender: User?, senderError) = userRepository.getOrCreateUsingDictionary(senderDict, AndSync: true)
      
      if senderError != nil {
        logger.error("Unable to create message because an error ocurred while getting sender: \(senderError!.extendedDescription)")
        return (nil, senderError)
      } else if divisionError != nil {
        logger.error("Unable to create message because an error ocurred while getting receiving division: \(divisionError!.extendedDescription)")
        return (nil, divisionError)
      } else if let division = division,
        sender = sender
      {
        message.id = id
        message.content = content
        message.timestamp = timestamp
        message.sender = sender
        message.division = division
        
        // If this is the first message, or the message is newer use it
        if division.latestMessage == nil || division.latestMessage?.timestamp?.compare(timestamp) == NSComparisonResult.OrderedAscending {
          logger.debug("Message \(id) is newest in division \(division.id), updating division's latest message")
          division.latestMessage = message
        }
        return ((message as! T), nil)
      } else {
        let error = MVError.createError(MVErrorCodes.MVMessageCreationError)
        logger.error("Unable to create message: \(error.extendedDescription)")
        return (nil , error)
      }
    } else {
      let error = MVError.createError(.MVServerResponseParseError)
      logger.error("Unable to create message: \(error.extendedDescription)")
      return (nil, error)
    }
  }
  
  /// This function creates a new message using the provided information.
  func createMessage(content: String, timestamp: NSDate, division: Division, sender: User) -> Message {
    logger.verbose("Creating message with content \(content), timestamp \(timestamp), sender \(sender.id) and division \(division.id)")
    let newItem: Message = createObjectWithId(NSUUID().UUIDString, AndSync: false)
    newItem.content = content
    newItem.timestamp = timestamp
    newItem.division = division
    newItem.sender = sender
    // Setting read flag to false, since messages send by the user are not re-synced.
    newItem.read = false
    
    // If this is the first message, or the message is newer use it
    if division.latestMessage == nil || division.latestMessage?.timestamp?.compare(timestamp) == NSComparisonResult.OrderedAscending {
      logger.debug("Message \(newItem) is newest in division \(division.id), updating division's latest message")
      division.latestMessage = newItem
    }
    return newItem
  }
}