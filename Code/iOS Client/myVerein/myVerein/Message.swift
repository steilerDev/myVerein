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
//  Message.swift
//  This file holds all information related to the message object of the application
//

import Foundation
import CoreData
import JSQMessagesViewController
import SwiftyUserDefaults

// MARK: - Pure database object, holding only information stored in database
class Message: NSManagedObject {
  @NSManaged var id: String
  @NSManaged var read: Bool
  @NSManaged var timestamp: NSDate!
  @NSManaged var sender: User!
  @NSManaged var division: Division!
  @NSManaged var content: String!
}

// MARK: - Convenience getter and setter for complex values/relations stored in database
extension Message {
  var isOutgoingMessage: Bool {
    if let userID = Defaults[MVUserDefaultsConstants.UserID].string,
      senderID = sender?.id
    {
      return userID == senderID
    } else {
      return true
    }
  }
}

// MARK: - MVCoreDataObject protocol functions
extension Message: CoreDataObject {
  static var remoteId: String { return MessageConstants.RemoteMessage.Id }
  static var className: String { return MessageConstants.ClassName }
  
  var syncRequired: Bool {
    return sender == nil || division == nil || timestamp == nil || content == nil
  }
  
  func sync() {
    MVNetworkingHelper.syncMessage(id)
  }
}

// MARK: - JSQMessageData protocol functions
extension Message: JSQMessageData {
  func date() -> NSDate {
    return timestamp
  }
  
  func senderDisplayName() -> String {
    if let sender = sender {
      if let firstName = sender.firstName, lastName = sender.lastName {
        return "\(firstName) \(lastName)"
      } else if let email = sender.email {
        sender.sync()
        return email
      } else {
        sender.sync()
        return sender.id
      }
    } else {
      sync()
      return "Sender not available"
    }
  }
  
  func senderId() -> String {
    return sender.id
  }
  
  func text() -> String {
    return content
  }
  
  func isMediaMessage() -> Bool {
    return false
  }
  
  func messageHash() -> UInt {
    return UInt(abs(content?.hash ?? 0) ^ abs(timestamp!.hash ?? 0))
  }
}

// MARK: - Printable protocol function
extension Message: Printable {
  override var description: String {
    if let content = content, timestamp = timestamp {
      return "Message from \(sender) to \(division): \(content) [\(timestamp)]"
    } else {
      return "Message from \(sender) to \(division): \(id)"
    }
  }
}

// MARK: - Message object related constants
struct MessageConstants {
  static let ClassName = "Message"
  static let BatchSize = 35
  
  // This struct defines the names of all database columns
  struct Fields {
    static let Timestamp = "timestamp"
    static let Read = "read"
    static let Division = "division"
    static let Id = "id"
  }
  
  // This struct defines the names of the member fields on the remote object. They are used to parse the event.
  struct RemoteMessage {
    static let Content = "content"
    static let Division = "group"
    static let Id = "id"
    static let Sender = "sender"
    static let Timestamp = "timestamp"
  }
}
