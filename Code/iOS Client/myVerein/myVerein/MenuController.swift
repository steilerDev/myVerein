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

import UIKit
import SwiftyUserDefaults
import XCGLogger

class MenuController: UITabBarController {
  
  let logger = XCGLogger.defaultInstance()
  
  /// Changing the color of the tab bar icons
  override func viewDidLoad() {
    super.viewDidLoad()
    UITabBar.appearance().tintColor = UIColor(hex: MVColor.Primary.Normal)
  }
  
  /// After the view appeared check if there are any credentials stored and if they are still valid. If they are not the login view is presented
  override func viewDidAppear(animated: Bool) {
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
      MVNetworking.loginActionWithCallbackOnMainQueue(
        success: {
          XCGLogger.info("Login successfully, no need to ask for credentials")
        },
        failure:
        {
          error in
          XCGLogger.error("Login was unsuccessfully: \(error.localizedDescription)")
        }
      )
    }
  }
}
