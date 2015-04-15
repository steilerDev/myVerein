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
//  MVHelper.swift
//  This file holds independent reusable classes written by myself, including extensions for existing classes, e.g. an extension for bound-save array access, or delegate protocol definitions.
//

import Foundation
import CoreData
import UIKit

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

// MARK: - NSError extension
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

// MARK: - UIImageView extension
/// This UIImageView extension implements the possible assignment of an object's image representation to an UIImageView
extension UIImageView {
  /// This function assigns the image representation of the user to the UIImageView.
  ///
  /// :param: user The user, who should be represented within the UIImageView.
  func setImageWithUser(user: User?) {
    if let userAvatar = user?.avatar {
      self.image = userAvatar
    } else {
      self.setImageWithString(user?.displayName ?? "N/A", color: UIColor(hex: MVColor.Primary.Normal))
    }
  }
  
  /// This function assigns the image representation of the division to the UIImageView.
  ///
  /// :param: division The division, which should be represented within the UIImageView.
  func setImageWithDivision(division: Division?) {
    self.setImageWithString(division?.name ?? "N/A", color: UIColor(hex: MVColor.Primary.Normal))
  }
}

// MARK: - UIViewController extension
/// This UIViewController extension provides a way to retrieve the currently shown viewcontroller inside a navigation view controller, or the viewcontroller itself, if the view controller is not a navigation view controller
extension UIViewController {
  /// This function returns the content view controller of the specified view controller. This means that a view controller, which is embedded in an navigation controller would be returned instead of the root navigation view controller. The same holds for a tab bar controller.
  var contentViewController: UIViewController {
    if let parentNavigationViewController = self as? UINavigationController {
      return parentNavigationViewController.visibleViewController
    } else if let parentTabBarViewController = self as? UITabBarController {
      return parentTabBarViewController.selectedViewController!
    } else {
      return self
    }
  }
}