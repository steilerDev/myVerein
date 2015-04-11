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

class MessageRepository: MVCoreDataRepository {
  
  // MARK: - Functions used to query the database
  
  // This function gathers all messages send through the division's chat
  func findMessagesBy(#division: Division) -> [Message]? {
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
  
  func findMessageBy(#id: String) -> Message? {
    logger.verbose("Getting message with id \(id)")
    // Create a new fetch request using the Message entity
    let fetchRequest = NSFetchRequest(entityName: MessageConstants.ClassName)
    fetchRequest.fetchBatchSize = MessageConstants.BatchSize
    
    let predicate = NSPredicate(format: "\(MessageConstants.Fields.Id) == %@", id)
    fetchRequest.predicate = predicate
    
    // Execute the fetch request, and cast the results to an array of LogItem objects
    return executeListRequest(fetchRequest)?.last
  }
  
  // MARK: - Creation and population of message
  
  /// This function tries to parse an array of messages and inserts them temporarily into the database.
  func getOrCreateMessagesFrom(#serverResponseObject: [AnyObject]) -> (messages: [Message]?, error: NSError?) {
    logger.verbose("Creating message from response object: \(serverResponseObject)")
    var newMessages = [Message]()
    for message in serverResponseObject {
      
      if let messageDict = message as? [String: AnyObject] {
        let (newMessage, error) = getOrCreateMessageFrom(serverResponseObject: messageDict)
        if error != nil && newMessage == nil {
          return (nil, error)
        } else {
          newMessages.append(newMessage!)
        }
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to parse messages: \(error.localizedDescription)")
        return (nil, error)
      }
    }
    logger.info("Returning \(newMessages.count) new messages")
    return (newMessages, nil)
  }
  
  /// This function tries to parse a message and inserts it temporarily into the database.
  func getOrCreateMessageFrom(#serverResponseObject: [String: AnyObject]) -> (message: Message?, error: NSError?) {
    if let id = serverResponseObject[MessageConstants.RemoteMessage.Id] as? String {
      if let message = findMessageBy(id: id) {
        return (message, nil)
      } else if let content = serverResponseObject[MessageConstants.RemoteMessage.Content] as? String,
        timestampDict = serverResponseObject[MessageConstants.RemoteMessage.Timestamp] as? Dictionary<String, AnyObject>,
        timestamp = MVDateParser.parseDateTime(timestampDict),
        divisionDict = serverResponseObject[MessageConstants.RemoteMessage.Division] as? Dictionary<String, AnyObject>,
        senderDict = serverResponseObject[MessageConstants.RemoteMessage.Sender] as? Dictionary<String, AnyObject>
      {
        let divisionRepository = DivisionRepository()
        let userRepository = UserRepository()
        let (divisionOptional, divisionError) = divisionRepository.getOrCreateDivisionFrom(serverResponseObject: divisionDict)
        let (senderOptional, senderError) = userRepository.getOrCreateUserFrom(serverResponseObject: senderDict)
        
        if senderError != nil {
          logger.error("Unable to create message because an error ocurred while getting sender: \(senderError?.localizedDescription)")
          return (nil, senderError)
        } else if divisionError != nil {
          logger.error("Unable to create message because an error ocurred while getting receiving division: \(divisionError?.localizedDescription)")
          return (nil, divisionError)
        } else if let division = divisionOptional,
          sender = senderOptional
        {
          logger.debug("Creating message with id \(id), content \(content), timestamp \(timestamp), sender \(sender.id) and division \(division.id)")
          return (createMessage(content, id: id, timestamp: timestamp, division: division, sender: sender), nil)
        } else {
          let error = MVError.createError(MVErrorCodes.MVMessageCreationError)
          logger.error("Unable to create message: \(error.localizedDescription)")
          return (nil , error)
        }
      } else {
        let error = MVError.createError(.MVServerResponseParseError)
        logger.error("Unable to create message: \(error.localizedDescription)")
        return (nil, error)
      }
    } else {
      let error = MVError.createError(.MVServerResponseParseError)
      logger.error("Unable to read message id: \(error.localizedDescription)")
      return (nil, error)
    }
  }
  
  /// This function creates a new message using the provided information.
  func createMessage(content: String, id: String, timestamp: NSDate, division: Division, sender: User) -> Message {
    logger.verbose("Creating message with id \(id), content \(content), timestamp \(timestamp), sender \(sender.id) and division \(division.id)")
    let newItem = NSEntityDescription.insertNewObjectForEntityForName(MessageConstants.ClassName, inManagedObjectContext: managedObjectContext) as! Message
    newItem.id = id
    newItem.content = content
    newItem.timestamp = timestamp
    newItem.division = division
    newItem.sender = sender
    newItem.read = false
    
    // If this is the first message, or the message is newer use it
    if division.latestMessage == nil || division.latestMessage?.timestamp?.compare(timestamp) == NSComparisonResult.OrderedAscending {
      logger.debug("Message \(id) is newest in division \(division.id), updating division's latest message")
      division.latestMessage = newItem
    }
    
    return newItem
  }
}