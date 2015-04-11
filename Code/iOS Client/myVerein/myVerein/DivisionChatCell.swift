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
//  DivisionChatCell.swift
//  This file holds all information related to a single division chat cell, shown in the division chat overview. The file provides loading mechanisms for displaying the correct information, as well as managing the badges for the chat.
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