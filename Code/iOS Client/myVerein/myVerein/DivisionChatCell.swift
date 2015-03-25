//
//  DivisionChatCell.swift
//  myVerein
//
//  Created by Frank Steiler on 03/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import UIImageView_Letters
import RKNotificationHub
import XCGLogger

class DivisionChatCell: UICollectionViewCell {
  
  let logger = XCGLogger.defaultInstance()
  
  lazy var notificationBadge: RKNotificationHub = {
    RKNotificationHub(view: self.divisionImageView.superview)
  }()
  
  var notificationCount: Int {
    get {
      return Int(notificationBadge.count())
    }
    set {
      notificationBadge.setCount(Int32(newValue))
      notificationBadge.pop()
    }
  }
  
  @IBOutlet weak var divisionImageView: UIImageView! {
    didSet {
      
    }
  }
  
  @IBOutlet weak var divisionLabel: UILabel!
  
  var division: Division? {
    didSet {
      if let division = division {
        if let divisionName = division.name {
          divisionLabel.text = divisionName
        } else {
          division.sync()
        }
      } else {
        XCGLogger.error("Unable to configure cell using default values")
        divisionLabel.text = "Division"
      }
    }
  }
  
  func updateAvatarView() {
    logger.debug("Updating avatar view")
    if let message = division?.latestMessage
    {
      logger.debug("Succesfully parsed latest message")
      if let userAvatar = message.sender.avatar {
        logger.debug("Using avatar image")
        divisionImageView.image = userAvatar
      } else {
        logger.debug("Using initials")
        divisionImageView.setImageWithString(message.sender.displayName, color: UIColor(hex: MVColor.Primary.Normal))
      }
    } else if let divisionName = division?.name {
      logger.error("Unable to get latest message, using division initials")
      divisionImageView.setImageWithString(divisionName, color: UIColor(hex: MVColor.Primary.Normal))
    } else {
      logger.error("Unable to get division name")
      divisionImageView.setImageWithString("N/A", color: UIColor(hex: MVColor.Primary.Normal))
    }
    divisionImageView.layer.borderWidth = 2
    divisionImageView.layer.borderColor = UIColor(hex: MVColor.Primary.Normal).CGColor
    divisionImageView.layer.cornerRadius = divisionImageView.layer.bounds.size.width/2
    divisionImageView.layer.masksToBounds = true
    // If this attribute is not set a bunch of auto layout warnings will appear
    divisionImageView.superview?.superview?.setTranslatesAutoresizingMaskIntoConstraints(false)
  }
  
}