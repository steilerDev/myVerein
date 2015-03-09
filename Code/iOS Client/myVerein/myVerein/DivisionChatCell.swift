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
            divisionImageView.setImageWithString(lastUser!, color: UIColor(hex: 0x3AD18C))
            divisionImageView.layer.borderWidth = 2
            divisionImageView.layer.borderColor = UIColor(hex: 0x13CD78).CGColor
            divisionImageView.layer.cornerRadius = divisionImageView.layer.bounds.size.width/2
            divisionImageView.layer.masksToBounds = true
            // If this attribute is not set a bunch of auto layout warnings will appear
            divisionImageView.superview?.superview?.setTranslatesAutoresizingMaskIntoConstraints(false)
        }
    }    
}

extension UIColor {
    convenience init(hex: Int) {
        self.init(
            red: (CGFloat((hex & 0xFF0000) >> 16) / 255.0),
            green: (CGFloat((hex & 0x00FF00) >> 8) / 255.0),
            blue: (CGFloat((hex & 0x0000FF) >> 0) / 255.0),
            alpha: 1.0
        )
    }
}