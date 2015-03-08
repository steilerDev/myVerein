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
    }
    
    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
    
    /// This function creates a new message using the provided information.
    func createMessage(content: String, id: String, timestamp: NSTimeInterval, division: Division, sender: User) -> Message {
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
    
    /// This function saves the 
    func save() {
        var error : NSError?
        if managedObjectContext.save(&error) {
            println(error?.localizedDescription)
        }
    }
}