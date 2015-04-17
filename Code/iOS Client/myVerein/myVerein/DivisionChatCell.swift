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
  
  @IBOutlet weak var divisionLabel: UILabel!
  
  @IBOutlet weak var divisionImageView: UIImageView! {
    didSet {
      // Adjusting image view (Round image with primary border color)
      divisionImageView.layer.borderWidth = 2
      divisionImageView.layer.borderColor = UIColor(hex: MVColor.Primary.Normal).CGColor
      divisionImageView.layer.cornerRadius = divisionImageView.layer.bounds.size.width/2
      divisionImageView.layer.masksToBounds = true
      // If this attribute is not set a bunch of auto layout warnings will appear
      divisionImageView.superview?.superview?.setTranslatesAutoresizingMaskIntoConstraints(false)
    }
  }
  
  private(set) var division: Division?
  
  /// This function is configuring the cell using the provided division. It sets the label and updates the image view.
  func configureCell(division: Division?) {
    self.division = division
    if let division = division {
      if division.syncRequired {
        division.sync()
      }
      
      // This is going to be empty if sync is required, but should be filled as soon as the sync returns
      divisionLabel.text = division.name
      
      // Setting message bubble
      if let message = division.latestMessage {
        logger.debug("Successfully parsed latest message of division \(self.division): \(message)")
        divisionImageView.setImageWithUser(message.sender)
      } else {
        logger.error("Unable to get latest message of division \(self.division), showing division in image view")
        divisionImageView.setImageWithDivision(division)
      }
      
      // Getting notification count
      notificationCount = MessageRepository().countUnreadMessagesInDivision(division)
    } else {
      logger.error("Unable to get division, using default values")
      divisionLabel.text = "Division"
      divisionImageView.setImageWithDivision(nil)
    }
  }
}

// MARK: - NotificationCountDelegate methods
extension DivisionChatCell: NotificationCountDelegate {
  func decrementNotificationCountBy(amount: Int, sender: AnyObject?) {
    notificationCount--
  }
  
  func incrementNotificationCountBy(amount: Int, sender: AnyObject?) {
    notificationCount++
  }
  
  func updateNotificationCountTo(newCount: Int, sender: AnyObject?) {
    notificationCount = newCount
  }
}