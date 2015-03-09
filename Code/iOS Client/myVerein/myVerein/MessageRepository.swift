//
//  MessageRepository.swift
//  myVerein
//
//  Created by Frank Steiler on 05/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import UIKit
import CoreData
import XCGLogger

class MessageRepository {
    
    private struct MessageConstants {
        static let ClassName = "Message"
        static let TimestampField = "timestamp"
        static let ReadField = "read"
        static let DivisionField = "division"
        static let BatchSize = 35
        
        struct remoteMessage {
            static let Content = "content"
            static let Division = "group"
            static let Id = "id"
            static let Sender = "sender"
            static let Timestamp = "timestamp"
        }
    }
    
    private let logger = XCGLogger.defaultInstance()
    
    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
    
    /// This function tries to parse all messages within the array and store them temporarily into the database. 
    func parseMessageFrom(#serverResponseObject: Array<AnyObject>) -> (messages: [Message]?, error: NSError?) {
        logger.verbose("Creating message from response object: \(serverResponseObject)")
        var newMessages = [Message]()
        for message in serverResponseObject {
            if let messageDict = message as? Dictionary<String, AnyObject>,
                    content = messageDict[MessageConstants.remoteMessage.Content] as? String,
                    id = messageDict[MessageConstants.remoteMessage.Id] as? String,
                    timestampDict = messageDict[MessageConstants.remoteMessage.Timestamp] as? Dictionary<String, AnyObject>,
                    timestamp = TimestampParser.parseTimestamp(timestampDict),
                    divisionDict = messageDict[MessageConstants.remoteMessage.Division] as? Dictionary<String, AnyObject>,
                    senderDict = messageDict[MessageConstants.remoteMessage.Sender] as? Dictionary<String, AnyObject>
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
                    newMessages.append(createMessage(content, id: id, timestamp: timestamp, division: division, sender: sender))
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
        }
        logger.info("Returning \(newMessages.count) new messages")
        return (newMessages, nil)
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
        return newItem
    }
    
    // This function gathers all messages send through the division's chat
    func findMessagesBy(#division: Division) -> [Message]? {
        logger.verbose("Getting all messages from database by division \(division.id)")
        // Create a new fetch request using the Message entity
        let fetchRequest = NSFetchRequest(entityName: MessageConstants.ClassName)
        fetchRequest.fetchBatchSize = MessageConstants.BatchSize
        let sortDescriptor = NSSortDescriptor(key: MessageConstants.TimestampField, ascending: true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        
        let predicate = NSPredicate(format: "\(MessageConstants.DivisionField) == %@", division)
        fetchRequest.predicate = predicate
        
        // Execute the fetch request, and cast the results to an array of LogItem objects
        return managedObjectContext.executeFetchRequest(fetchRequest, error: nil) as? [Message]
    }
    
    /// This function saves the database permanently
    func save() {
        logger.verbose("Saving database changes")
        var error : NSError?
        if managedObjectContext.save(&error) {
            logger.error("Unable to save database: \(error?.localizedDescription)")
        } else {
            logger.info("Successfully saved database")
        }
    }
}