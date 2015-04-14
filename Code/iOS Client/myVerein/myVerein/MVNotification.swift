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

class MVNotification {
  static let logger = XCGLogger.defaultInstance()
}

// MARK: - Send notifications
extension MVNotification {
  /// This function sends out a notification telling all subscriber that the message sync completed and that new messages have been retrieved.
  ///
  /// :param: sender Defines the sender of the notification.
  class func sendMessageSyncCompletedNotification(sender: AnyObject?) {
    logger.debug("Sending message sync completion notification")
    let notification = NSNotification(name: MVNotificationNames.MVMessageSyncCompleted, object: sender)
    NSNotificationCenter.defaultCenter().postNotification(notification)
  }
  
  /// This function sends out a notification telling all subscriber that the division sync completed and that the subscribed division changed (new divisions and/or less divisions)
  ///
  /// :param: sender Defines the sender of the notification.
  class func sendDivisionSyncCompletedNotification(sender: AnyObject?) {
    logger.debug("Sending division sync completion notification")
    let notification = NSNotification(name: MVNotificationNames.MVDivisionSyncCompleted, object: sender)
    NSNotificationCenter.defaultCenter().postNotification(notification)
  }
  
  /// This function sends out a notification telling all subscriber that the calendar sync completed and that new events have been retrieved and/or existing events have been altered.
  ///
  /// :param: sender Defines the sender of the notification.
  class func sendCalendarSyncCompletedNotification(sender: AnyObject?) {
    logger.debug("Sending calendar sync completion notification")
    let notification = NSNotification(name: MVNotificationNames.MVCalendarSyncCompleted, object: sender)
    NSNotificationCenter.defaultCenter().postNotification(notification)
  }
}

// MARK: - Subscribe to notifications
extension MVNotification {
  /// This function lets you subscribe to the message sync completed notification by defining a closure that should be executed upon receival of the notification. The closure is guaranteed to be executed on the main thread.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  class func subscribeToMessageSyncCompletedNotification(responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    logger.debug("Subscribing to message sync completion notification")
    return subscribeToNotification(MVNotificationNames.MVMessageSyncCompleted, responseClosure: responseClosure)
  }
  
  /// This function lets you subscribe to the division sync completed notification by defining a closure that should be executed upon receival of the notification. The closure is guaranteed to be executed on the main thread.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  class func subscribeToDivisionSyncCompletedNotification(responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    logger.debug("Subscribing to division sync completion notification")
    return subscribeToNotification(MVNotificationNames.MVDivisionSyncCompleted, responseClosure: responseClosure)
  }
  
  /// This function lets you subscribe to the calendar sync completed notification by defining a closure that should be executed upon receival of the notification. The closure is guaranteed to be executed on the main thread.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  class func subscribeToCalendarSyncCompletedNotification(responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    logger.debug("Subscribing to calendar sync completion notification")
    return subscribeToNotification(MVNotificationNames.MVCalendarSyncCompleted, responseClosure: responseClosure)
  }
  
  /// This function is subscribing to a notification defined by its name. The response closure is executed upon receival of the notification. The closure is guaranteed to be executed on the main thread.
  ///
  /// :param: responseClosure The closure that should be executed as soon as the notification is received.
  /// :param: name The name of the subscribed notification.
  /// :returns: A token associated with the notification, used to unsubscribe from the notification and therefore free resources. You should always unsubscribe from notification as soon as your subscriber is no longer needed, because the notification is possibly causing memory leaks because of captured elements within the response closure.
  private class func subscribeToNotification(name: String, responseClosure: (NSNotification!) -> ()) -> NSObjectProtocol {
    let mainQueue = NSOperationQueue.mainQueue()
    return NSNotificationCenter.defaultCenter().addObserverForName(name, object: nil, queue: mainQueue, usingBlock: responseClosure)
  }
}

// MARK: - Un-subscribe from notifications
extension MVNotification {
  /// This function is used to unsubscribe from a notification defined by the token. By unsubscribing from the notification the resources captured by the response closure are freed.
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