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
//  MenuController.swift
//  This file holds all information related to the tab bar view of the main menu. It modifies the appearance to match the experience as well as validating the stored credentials.
//

// TODO: Accept notification about synced messages

import UIKit
import SwiftyUserDefaults
import XCGLogger

class MenuController: UITabBarController {
  
  let logger = XCGLogger.defaultInstance()
  
  /// Changing the color of the tab bar icons
  override func viewDidLoad() {
    super.viewDidLoad()
    UITabBar.appearance().tintColor = UIColor(hex: MVColor.Primary.Normal)
    
    // Subscribing to notifications, no need to un-subscribe since this view controller is always around
    MVNotification.subscribeToCalendarSyncCompletedNotificationForEvent(nil) {
      _ in
      self.calendarViewControllerItem.updateBadgeCount(EventRepository().countPendingEvents())
    }
    MVNotification.subscribeToMessageSyncCompletedNotificationForDivisionChat(nil){
      _ in
      self.chatViewControllerItem.updateBadgeCount(MessageRepository().countUnreadMessages())
    }
  }
  
  /// After the view appeared perform actions to update UI and check log in status
  override func viewDidAppear(animated: Bool) {
    //Check current credentials
    let (currentUsername, currentPassword, currentDomain) = MVSecurity.instance().currentKeychain()
    
    if !Defaults.hasKey(MVUserDefaultsConstants.UserID) ||
      currentUsername == nil ||
      currentPassword == nil ||
      currentDomain == nil
    {
      logger.info("No credentials stored, asking the user to enter his")
      (UIApplication.sharedApplication().delegate as! AppDelegate).showLoginView()
    } else {
      logger.info("Credentials found, checking if they are valid")
      MVNetworking.defaultInstance().performLogIn(
        success: {
          XCGLogger.info("Login successfully, no need to ask for credentials")
        },
        failure:
        {
          error in
          XCGLogger.error("Login was unsuccessfully: \(error.extendedDescription)")
        }
      )
    }
    
    // Subscribing to notifications, no need to un-subscribe since this view controller is always around
    MVNotification.subscribeToCalendarSyncCompletedNotificationForEvent(nil) {
      if $0.object == nil {
        self.calendarViewControllerItem.updateBadgeCount(EventRepository().countPendingEvents())
      }
    }
    MVNotification.subscribeToMessageSyncCompletedNotificationForDivisionChat(nil){
      if $0.object == nil {
        self.chatViewControllerItem.updateBadgeCount(MessageRepository().countUnreadMessages())
      }
    }
    
    // Update count on tabbar items
    calendarViewControllerItem.updateBadgeCount(EventRepository().countPendingEvents())
    chatViewControllerItem.updateBadgeCount(MessageRepository().countUnreadMessages())
  }
}

// MARK: - Convenience getter for tabbar items
extension MenuController {
  var calendarViewControllerItem: UITabBarItem {
    return tabBar.items![1] as! UITabBarItem
  }
  
  var chatViewControllerItem: UITabBarItem {
    return tabBar.items![0] as! UITabBarItem
  }
}

// MARK: - NotificationCountDelegate
extension MenuController: NotificationCountDelegate {
  func incrementNotificationCountBy(amount: Int, sender: AnyObject?) {
    if sender is CalendarViewController {
      calendarViewControllerItem.incrementBadgeCountBy(amount)
    } else if sender is DivisionChatOverviewViewController {
      chatViewControllerItem.incrementBadgeCountBy(amount)
    }
  }
  
  func decrementNotificationCountBy(amount: Int, sender: AnyObject?) {
    incrementNotificationCountBy(-amount, sender: sender)
  }
  
  func updateNotificationCountTo(newCount: Int, sender: AnyObject?) {
    if sender is CalendarViewController {
      calendarViewControllerItem.updateBadgeCount(newCount)
    } else if sender is DivisionChatOverviewViewController {
      chatViewControllerItem.updateBadgeCount(newCount)
    }
  }
}
