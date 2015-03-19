//
//  Message.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData
import JSQMessagesViewController

class Message: NSManagedObject {
  @NSManaged var content: String?
  @NSManaged var id: String
  @NSManaged var read: Bool
  @NSManaged var timestamp: NSDate
  @NSManaged var division: Division
  @NSManaged var sender: User
}

// MARK: - MVCoreDataObject
extension Message: MVCoreDataObject {
  var syncRequired: Bool {
    return false
  }
  
  func sync() {
    MVNetworkingHelper.syncMessages()
  }
}

// MARK: - JSQMessageData
extension Message: JSQMessageData {
  func date() -> NSDate {
    return timestamp
  }
  
  func senderDisplayName() -> String {
    if let firstName = sender.firstName, lastName = sender.lastName {
      return "\(firstName) \(lastName)"
    } else if let email = sender.email {
      sender.sync()
      return email
    } else {
      sender.sync()
      return sender.id
    }
  }
  
  func senderId() -> String {
    return sender.id
  }
  
  func text() -> String {
    return content!
  }
  
  func isMediaMessage() -> Bool {
    return false
  }
  
  func messageHash() -> UInt {
    return UInt(content?.hash ?? 0 ^ timestamp.hash ?? 0)
  }
}
