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
  
  @IBOutlet weak var divisionImageView: UIImageView!
  
  @IBOutlet weak var divisionLabel: UILabel!
  
  var lastUser: String? {
    didSet {
      divisionImageView.setImageWithString(lastUser ?? "", color: UIColor(hex: MVColor.Primary.Normal))
      divisionImageView.layer.borderWidth = 2
      divisionImageView.layer.borderColor = UIColor(hex: MVColor.Primary.Normal).CGColor
      divisionImageView.layer.cornerRadius = divisionImageView.layer.bounds.size.width/2
      divisionImageView.layer.masksToBounds = true
      // If this attribute is not set a bunch of auto layout warnings will appear
      divisionImageView.superview?.superview?.setTranslatesAutoresizingMaskIntoConstraints(false)
    }
  }
  
  var division: Division? {
    didSet {
      if let division = division {
        if let divisionName = division.name {
          divisionLabel.text = divisionName
          lastUser = divisionName
        } else {
          division.sync()
        }
      } else {
        XCGLogger.error("Unable to configure cell using default values")
        divisionLabel.text = "Division"
        lastUser = "Division"
      }
    }
  }
}