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
//  SupportingClasses.swift
//  This file holds independent reusable classes written by myself, including an improved multi-tasking operator and an extension for bound-save array access.
//

import Foundation
import CoreData
import UIKit

/// MARK: - Marshal operator

// This part introduces a new infix operator: '~>'. It will be called the marshal operator. It defines the execution of two closures after each other on different threads. The operator is implemented according to Josh Smith's suggestion: http://ijoshsmith.com/2014/07/05/custom-threading-operator-in-swift/

// The operator can be used infix, prefix and postfix, where infix defines a background task followed by a main queue task, prefix defines a main queue tast and postfix defines a background task. Therefore the operator handles all threading tasks.
infix operator ~> {}
prefix operator ~> {}
postfix operator ~> {}

// Serial dispatched queue used by ~>
private let queue = dispatch_queue_create("serial-worker", DISPATCH_QUEUE_SERIAL)

/// Using the Marshal operator as a prefix operator, means that the closure is executed on the main thread.
///
/// :param: mainClosure The closure executed on the main thread.
prefix func ~> (mainClosure: () -> ()) {
  dispatch_async(dispatch_get_main_queue(), mainClosure)
}

/// Using the Marshal operator as a postfix operator, means that the closure is executed on a background thread.
///
/// :param: backgroundClosure The closure executed on a background thread.
postfix func ~> (backgroundClosure: () -> ()) {
  dispatch_async(queue, backgroundClosure)
}

// Using the Marshal operator as a postfix operator, means that the closure is executed on a background thread. This operator provides a managed object context created on the background queue and can therefore be used without any concurency problems.
///
/// :param: backgroundClosure The closure executed on a background thread providing a managed context usable on this thread.
postfix func ~> (backgroundClosure: (NSManagedObjectContext) -> ()) {
  dispatch_async(queue) {
    var backgroundContext = NSManagedObjectContext()
    backgroundContext.persistentStoreCoordinator = (UIApplication.sharedApplication().delegate as! AppDelegate).persistentStoreCoordinator!
    backgroundClosure(backgroundContext)
  }
}

/// Executes the left-hand closure on the background thread, upon completion the right-hand closure is executed on the main thread.
///
/// :param: backgroundClosure The closure executed on a background thread.
/// :param: mainClosure The closure executed on the main thread, after the background thread is finished.
func ~> (backgroundClosure: () -> (), mainClosure: () -> ()) {
  dispatch_async(queue) {
    backgroundClosure()
    dispatch_async(dispatch_get_main_queue(), mainClosure)
  }
}

/// Executes the left-hand closure on the background thread, upon completion the right-hand closure is executed on the main thread using the return value of the left-hand closure.
///
/// :param: backgroundClosure The closure executed on a background thread.
/// :param: mainClosure The closure executed on the main thread.
func ~> <R> (backgroundClosure: () -> (R), mainClosure: (R) -> ()) {
  dispatch_async(queue) {
    let result = backgroundClosure()
    dispatch_async(dispatch_get_main_queue()) {
      mainClosure(result)
    }
  }
}

// MARK: - Array extension
/// An array extension providing a bound-save getter using swift's optionals
extension Array {
  
  /// This function returns the object at the provided index. If the index is out of bounds nil is returned.
  ///
  /// :param: index The index of the object, the user wants to retrieve.
  ///
  /// :returns: The object, if the index is not out of bounds, nil otherwise.
  func get(index: Int) -> T? {
    return 0 <= index && index < count ? self[index]: nil
  }
}

// MARK: = NSError extension
/// An NSError extension providing a unified and concise string representation of the error
extension NSError {
  /// This variable provides an extended string representation of the ocurred error.
  var extendedDescription: String {
    var tempDescription = "Error (\(code) in \(domain)): \(localizedDescription)"
    if let recoverySuggestion = localizedRecoverySuggestion {
      tempDescription += " (\(recoverySuggestion))"
    }
    if let failingURL = userInfo?[NSURLErrorFailingURLStringErrorKey] as? String {
      tempDescription += " -> Failed URL \(failingURL)"
    }
    if let underlyingError = userInfo?[NSUnderlyingErrorKey] as? NSError {
      tempDescription += " [Underlying error: \(underlyingError.extendedDescription)]"
    }
    return tempDescription
  }
}

// MARK: - Notification count delegate
/// This delegate defines a protocol used to notify a delegate about the change of a notification count related to the sender. Depending on the data structure either of the function (increment & decrement or update) is used by the sender.
/// In the context of this application the protocol is used to notify a parent view about a data change in the child view that affects the number shown in a badge on the parent view.
protocol NotificationCountDelegate {
  /// This function should be called after the change in the data structure occured and before dismissing the view controller.
  ///
  /// :param: amount The amount of units, the notification count should be decremented on the delegate.
  /// :param: sender The sender of the method, mainly used to identify which count needs to be updated.
  func decrementNotificationCountBy(amount: Int, sender: AnyObject?) -> ()
  /// This function should be called after the change in the data structure occured and before dismissing the view controller.
  ///
  /// :param: amount The amount of units, the notification count should be incremented on the delegate.
  /// :param: sender The sender of the method, mainly used to identify which count needs to be updated.
  func incrementNotificationCountBy(amount: Int, sender: AnyObject?) -> ()
  /// This function should be called after the change in the data structure occured and before dismissing the view controller.
  ///
  /// :param: newCount The updated notification count of the delegate.
  /// :param: sender The sender of the method, mainly used to identify which count needs to be updated.
  func updateNotificationCountTo(newCount: Int, sender: AnyObject?) -> ()
}

// MARK: - UITabBarItem extension
/// A UITabBarItem extension giving the ability to increase, decrease or update the notification count if it is a number
extension UITabBarItem {
  /// This function decrements the badge count by the provided amount of units. If there is no badge, one gets set, if the result of the substraction is zero the badge gets removed.
  ///
  /// :param: amount The amount of units, the badge count should be decremented.
  func decrementBadgeCountBy(amount: Int) {
    incrementBadgeCountBy(-amount)
  }
  
  /// This function increments the badge count by the provided amount of units. If there is no badge, one gets set, if the result of the addition is zero the badge gets removed.
  ///
  /// :param: amount The amount of units, the badge count should be incremented.
  func incrementBadgeCountBy(amount: Int) {
    if let badgeValue = badgeValue where !badgeValue.isEmpty {
      if let badgeInt = badgeValue.toInt() {
        updateBadgeCount(badgeInt + amount)
      }
    } else {
      // If there is no badge string put the new amount into it
      badgeValue = String(amount)
    }
  }
  
  /// This function removes the badge.
  func clearBadgeCount() {
    updateBadgeCount(0)
  }
  
  /// This function sets the badge count to the provided value. If the value is zero the badge gets removed.
  ///
  /// :param: newValue The value the badge should show.
  func updateBadgeCount(newValue: Int) {
    if newValue == 0 {
      badgeValue = nil
    } else {
      badgeValue = String(newValue)
    }
  }
}