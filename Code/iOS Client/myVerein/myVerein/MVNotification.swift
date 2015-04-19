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
//  MVNotification.swift
//  This file creates an abstraction layer to send, subscribe and unsubscribe from notification.
//

import Foundation
import XCGLogger

/// This class should provide convenience subscribe and send mechanisms for notifications send within the application. The main goal is to notify views specially about the change in their data structure. The controller should then be able to adjust its view according to the new information. This mechanisms is mainly introduced to add a support for changing porperties that are not monitored by the NSFetchedResultControllers throughout the application and views that do not monitor the data structure at all.
class MVNotification {
  static let logger = XCGLogger.defaultInstance()
}

// MARK: - Send notifications
extension MVNotification {
  /// This function sends out a notification telling all subscriber that the message sync completed and that new messages have been retrieved. A notification for each new message is send out, where the sender is the division of the message. Finally a a notification with an empty sender is posted, to signal that all new messages have been proccessed.
  ///
  /// :param: messages An array of new events that are the reason why this notification is send.
  class func sendMessageSyncCompletedNotificationForNewMessage(message: Message) {
    logger.debug("Sending message sync completion notification for \(message)")
    let notificationCenter = NSNotificationCenter.defaultCenter()
    let notification = NSNotification(name: MVNotificationNames.MVMessageSyncCompleted, object: message.division)
    notificationCenter.postNotification(notification)
  }
  
  /// This function sends out a notification telling all subscriber that the division sync completed and that the subscribed division changed (new divisions and/or less divisions). A notification for each changed division is send out, where the sender is the division itself. After the batch of changed divisions has been processed a notification with an empty sender is posted, to signal that all divisions have been processed.
  ///
  /// :param: divisions An array of new/changed divisions that are the reason why this notification is send.
  class func sendDivisionSyncCompletedNotificationForChangedDivisions(divisions: [Division]) {
    logger.debug("Sending division sync completion notification for \(divisions)")
    let notificationCenter = NSNotificationCenter.defaultCenter()
    for division in divisions {
      let notification = NSNotification(name: MVNotificationNames.MVDivisionSyncCompleted, object: division)
      notificationCenter.postNotification(notification)
    }
    let finalNotification = NSNotification(name: MVNotificationNames.MVDivisionSyncCompleted, object: nil)
    notificationCenter.postNotification(finalNotification)
  }
  
  /// This function sends out a notification telling all subscriber that the calendar sync completed and that new events have been retrieved and/or existing events have been altered. A notification for each changed event is send out, where the sender is the event itself. After the batch of changed events has been processed a notification with an empty sender is posted, to singal that the bach of events has been processed.
  ///
  /// :param: events An array of new/changed events that are the reason why this notification is send.
  class func sendCalendarSyncCompletedNotificationForChangedEvent(event: Event) {
    logger.debug("Sending calendar sync completion notification for \(event)")
    let notificationCenter = NSNotificationCenter.defaultCenter()
    let notification = NSNotification(name: MVNotificationNames.MVCalendarSyncCompleted, object: event)
    notificationCenter.postNotification(notification)
  }
}

// MARK: - Subscribe to notifications
extension MVNotification {
  /// This function lets you subscribe to the message sync completed notification by defining a closure that should be executed upon receival of the notification. The closure is guaranteed to be executed on the main thread. In general you subscribe to the notification within the 'viewDidAppear' function and un-subscribe within the 'viewWillDisappear' function to efficiently release unnecessary resources.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :param: division The division's chat that should be monitored. This parameter is used to filter out irrelevant notifications. If this parameter is nil all notifications of this type are received.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  class func subscribeToMessageSyncCompletedNotificationForDivisionChat(division: Division?, responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    logger.debug("Subscribing to message sync completion notification for division chat \(division)")
    return subscribeToNotification(MVNotificationNames.MVMessageSyncCompleted, sender: division, responseClosure: responseClosure)
  }
  
  /// This function lets you subscribe to the division sync completed notification by defining a closure that should be executed upon receival of the notification. The closure is guaranteed to be executed on the main thread. In general you subscribe to the notification within the 'viewDidAppear' function and un-subscribe within the 'viewWillDisappear' function to efficiently release unnecessary resources.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :param: division The division that should be monitored. This parameter is used to filter out irrelevant notifications. If this parameter is nil all notifications of this type are received.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  class func subscribeToDivisionSyncCompletedNotificationForDivision(division: Division?, responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    logger.debug("Subscribing to division sync completion notification for division \(division)")
    return subscribeToNotification(MVNotificationNames.MVDivisionSyncCompleted, sender: division, responseClosure: responseClosure)
  }
  
  /// This function lets you subscribe to the calendar sync completed notification by defining a closure that should be executed upon receival of the notification. The closure is guaranteed to be executed on the main thread. In general you subscribe to the notification within the 'viewDidAppear' function and un-subscribe within the 'viewWillDisappear' function to efficiently release unnecessary resources.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :param: event The event that should be monitored. This parameter is used to filter out irrelevant notifications. If this parameter is nil all notifications of this type are received.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  class func subscribeToCalendarSyncCompletedNotificationForEvent(event: Event?, responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    logger.debug("Subscribing to calendar sync completion notification for event \(event)")
    return subscribeToNotification(MVNotificationNames.MVCalendarSyncCompleted, sender: event, responseClosure: responseClosure)
  }
  
  /// This function is subscribing to a notification defined by its name and sender object. The response closure is executed upon receival of the notification. The closure is guaranteed to be executed on the main thread. In general you subscribe to the notification within the 'viewDidAppear' function and un-subscribe within the 'viewWillDisappear' function to efficiently release unnecessary resources.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :param: name The name of the subscribed notification.
  /// :param: sender Only notfications of this object will be received. The objects are compared using the id's of the CoreDataObject protocol. If there is no sender defined, all notification will be provided.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  private class func subscribeToNotification(name: String, sender: CoreDataObject?, responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    let mainQueue = NSOperationQueue.mainQueue()
    if let sender = sender {
      let newResponseClosure: (NSNotification!) -> () = {
        notification in
        if let notificationSender = notification.object as? CoreDataObject where notificationSender.id == sender.id {
          XCGLogger.debug("Executing notification for \(notificationSender)")
          responseClosure(notification)
        } else {
          XCGLogger.debug("Not executing notification because its sender is not the subscribed object \(sender)")
        }
      }
      logger.debug("Subscribing to notification, filtering with \(sender)")
      return NSNotificationCenter.defaultCenter().addObserverForName(name, object: nil, queue: mainQueue, usingBlock: newResponseClosure)
    } else {
      logger.debug("Subscribing to notification, without filtering")
      return NSNotificationCenter.defaultCenter().addObserverForName(name, object: nil, queue: mainQueue, usingBlock: responseClosure)
    }
  }
}

// MARK: - Un-subscribe from notifications
extension MVNotification {
  /// This function is used to unsubscribe from a notification defined by the token. By unsubscribing from the notification the resources captured by the response closure are freed. In general you subscribe to the notification within the 'viewDidAppear' function and un-subscribe within the 'viewWillDisappear' function to efficiently release unnecessary resources.
  ///
  /// :param: notificationToken The token associated with a notification subscription, used to identify the subscription that should be freed.
  class func unSubscribeFromNotification(notificationToken: NSObjectProtocol) {
    NSNotificationCenter.defaultCenter().removeObserver(notificationToken)
  }
}

// MARK: - The constant names of the notifications
struct MVNotificationNames {
  static let MVMessageSyncCompleted = "mvMessageSyncCompleted"
  static let MVDivisionSyncCompleted = "mvDivisionSyncCompleted"
  static let MVCalendarSyncCompleted = "mvCalendarSyncCompleted"
}