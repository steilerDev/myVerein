//
//  MenuController.swift
//  myVerein
//
//  Created by Frank Steiler on 05/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import SwiftyUserDefaults
import XCGLogger

class MenuController: UITabBarController {
  
  override func viewDidLoad() {
    super.viewDidLoad()
    UITabBar.appearance().tintColor = UIColor(hex: MVColor.Primary.Normal)
  }
  
  // After the view appeared check if there are any credentials stored
  override func viewDidAppear(animated: Bool) {
    let (currentUsername, currentPassword, currentDomain) = MVSecurity.instance().currentKeychain()
    
    if !Defaults.hasKey(UserDefaultsConstants.UserID) ||
      currentUsername == nil ||
      currentPassword == nil ||
      currentDomain == nil
    {
      (UIApplication.sharedApplication().delegate as! AppDelegate).showLoginView()
    } else {
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
