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
    
    private struct MessageFields {
        static let messageClassName = "Message"
        static let messageContentFeld = "content"
        static let messageTimestampField = "timestamp"
        static let messageReadField = "read"
    }
    
    // Retreive the managedObjectContext from AppDelegate
    let managedObjectContext = (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!
    
    func createMessage(content: String) -> Message {
        let newItem = NSEntityDescription.insertNewObjectForEntityForName(MessageFields.messageClassName, inManagedObjectContext: managedObjectContext) as! Message
        newItem.content = content
        return newItem
    }
    
    func findMessageByChat(divisionName: String) -> [Message]? {
        // Create a new fetch request using the LogItem entity
        let fetchRequest = NSFetchRequest(entityName: MessageFields.messageClassName)
        let sortDescriptor = NSSortDescriptor(key: MessageFields.messageTimestampField, ascending: true)
        fetchRequest.sortDescriptors = [sortDescriptor]
        
        let predicate = NSPredicate(format: "title == %@", "Best Language")
        fetchRequest.predicate = predicate
        
        // Execute the fetch request, and cast the results to an array of LogItem objects
        return managedObjectContext.executeFetchRequest(fetchRequest, error: nil) as? [Message]
    }
    
    func save() {
        var error : NSError?
        if managedObjectContext.save(&error) {
            println(error?.localizedDescription)
        }
    }
}