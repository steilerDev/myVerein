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
    
    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
    
    func createMessage(serverResponseObject: Array<AnyObject>) -> (messages: [Message]?, error: NSError?) {
        
        var newMessages = [Message]()
        for message in serverResponseObject {
            println("Message: ===============================")
            println(message)
            println("========================================")
            
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
                let (divisionOptional, divisionError) = divisionRepository.getDivision(divisionDict)
                let (senderOptional, senderError) = userRepository.getUser(senderDict)
                        
                if senderError != nil {
                    return (nil, senderError)
                } else if divisionError != nil {
                    return (nil, divisionError)
                } else if let division = divisionOptional,
                                sender = senderOptional
                {
                    newMessages.append(createMessage(content, id: id, timestamp: timestamp, division: division, sender: sender))
                } else {
                    return (nil , MVError.createError(MVErrorCodes.MVMessageCreationError))
                }
            } else {
                return (nil, MVError.createError(.MVServerResponseParseError))
            }
        }
        return (newMessages, nil)
    }
    
    /// This function creates a new message using the provided information.
    func createMessage(content: String, id: String, timestamp: NSDate, division: Division, sender: User) -> Message {
        let newItem = NSEntityDescription.insertNewObjectForEntityForName(MessageConstants.ClassName, inManagedObjectContext: managedObjectContext) as! Message
        newItem.id = id
        newItem.content = content
        newItem.timestamp = timestamp
        newItem.division = division
        newItem.sender = sender
        return newItem
    }
    
    // This function gathers all messages send through the division's chat
    func findMessageByChat(division: Division) -> [Message]? {
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
        var error : NSError?
        if managedObjectContext.save(&error) {
            println(error?.localizedDescription)
        }
    }
}