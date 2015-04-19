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
//  MVDropdownAlert.swift
//  This file is including all protocols and functions to enable the use of in app dropdown alerts.
//  The class was inspired by Richard Kim's implentation (RKDropdownAlert): https://github.com/cwRichardKim/RKDropdownAlert
//

import Foundation
import UIKit

// MARK: - Dropdown alert class

class MVDropdownAlert: UIButton {
  
  // MARK: Static values manipulating the appearance
  struct DropdownAppearance {
    struct Padding {
      static let X: CGFloat = 10
      static let Y: CGFloat = 3
    }
    struct Time {
      static let Animation = 0.3
      static let Visible: Double = 3
    }
    struct Height {
      static let Dropdown: CGFloat = 90
      static let StatusBar: CGFloat = 21
      static let TopLabel: CGFloat = 20
      static let BottomLabel: CGFloat = 40
    }
    
    struct Font {
      static let TopLabel = UIFont.boldSystemFontOfSize(16)
      static let BottomLabel = UIFont.systemFontOfSize(14)
    }
    
    static let NumberOfLinesBottomLabel = 2
    
    struct Color {
      struct Default {
        static let Text = UIColor.whiteColor()
        static let Background = UIColor(hex: MVColor.Primary.Normal)
      }
      struct Danger {
        static let Text = UIColor.whiteColor()
        static let Background = UIColor(hex: MVColor.Danger.Normal)
      }
      struct Warning {
        static let Text = UIColor.whiteColor()
        static let Background = UIColor(hex: MVColor.Warning.Normal)
      }
      struct Info {
        static let Text = UIColor.whiteColor()
        static let Background = UIColor(hex: MVColor.Info.Normal)
      }
    }
  }
  
  let topLabel: UILabel
  let bottomLabel: UILabel
  
  var delegate: MVDropdownAlertDelegate?
  
  // MARK: Initializer
  
  override init(frame: CGRect) {
    let labelWidth = frame.size.width - (2 * DropdownAppearance.Padding.X)
    
    topLabel = UILabel(frame: CGRectMake(DropdownAppearance.Padding.X, DropdownAppearance.Height.StatusBar, labelWidth, DropdownAppearance.Height.TopLabel))
    topLabel.font = DropdownAppearance.Font.TopLabel
    topLabel.textAlignment = .Center
    
    bottomLabel = UILabel(frame: CGRectMake(DropdownAppearance.Padding.X, DropdownAppearance.Height.StatusBar + DropdownAppearance.Height.TopLabel + DropdownAppearance.Padding.Y, labelWidth, DropdownAppearance.Height.BottomLabel))
    bottomLabel.font = DropdownAppearance.Font.BottomLabel
    bottomLabel.lineBreakMode = .ByWordWrapping
    bottomLabel.numberOfLines = DropdownAppearance.NumberOfLinesBottomLabel
    bottomLabel.textAlignment = .Center
    
    super.init(frame: frame)
    
    self.addSubview(topLabel)
    self.addSubview(bottomLabel)
    
    self.addTarget(self, action: "didPressDropdown:", forControlEvents: .TouchUpInside)
  }

  required init(coder aDecoder: NSCoder) {
    topLabel = UILabel(coder: aDecoder)
    bottomLabel = UILabel(coder: aDecoder)
    super.init(coder: aDecoder)
  }
  
  convenience init() {
    self.init(frame: CGRectMake(0, -DropdownAppearance.Height.Dropdown, UIScreen.mainScreen().bounds.size.width, DropdownAppearance.Height.Dropdown))
  }
  
  // MARK: Showing the alert
  
  class func showAlert(alertObject: MVDropdownAlertObject) {
    MVDropdownAlert().showAlert(alertObject)
  }
  
  func showAlert(alertObject: MVDropdownAlertObject) {
    let textColor: UIColor
    let backgroundColor: UIColor
    switch alertObject.style {
      case .Info:
        textColor = DropdownAppearance.Color.Info.Text
        backgroundColor = DropdownAppearance.Color.Info.Background
      case .Default:
        textColor = DropdownAppearance.Color.Default.Text
        backgroundColor = DropdownAppearance.Color.Default.Background
      case .Warning:
        textColor = DropdownAppearance.Color.Warning.Text
        backgroundColor = DropdownAppearance.Color.Warning.Background
      case .Danger:
        textColor = DropdownAppearance.Color.Danger.Text
        backgroundColor = DropdownAppearance.Color.Danger.Background
    }
    
    showAlertWithTitle(alertObject.title, message: alertObject.message, backgroundColor: backgroundColor, textColor: textColor)
  }

  func showAlertWithTitle(title: String, message: String? = nil, backgroundColor: UIColor = DropdownAppearance.Color.Default.Background, textColor: UIColor = DropdownAppearance.Color.Default.Text, andDuration duration: Double = DropdownAppearance.Time.Visible) {
    topLabel.text = title
    
    if let message = message {
      bottomLabel.text = message
      if messageTextIsOneLine() {
        // Center labels if message text is one line
        topLabel.frame.origin.y += 6
        bottomLabel.frame.origin.y += 6
      }
    } else {
      // If there is no message, center it vertical
      topLabel.frame.size.height = DropdownAppearance.Height.Dropdown - 2 * DropdownAppearance.Padding.Y - DropdownAppearance.Height.StatusBar
      topLabel.frame.origin.y = DropdownAppearance.Padding.Y + DropdownAppearance.Height.StatusBar
    }
    
    self.backgroundColor = backgroundColor
    topLabel.textColor = textColor
    bottomLabel.textColor = textColor
    
    // Trying to assign the notification to the current top view
    if self.superview == nil {
      if let frontToBackWindows = UIApplication.sharedApplication().windows.reverse() as? [UIWindow] {
        for window in frontToBackWindows {
          if window.windowLevel == UIWindowLevelNormal && !window.hidden {
            window.addSubview(self)
            break
          }
        }
      }
    }
    
    UIView.animateWithDuration(DropdownAppearance.Time.Animation,
      animations: {
        self.frame.origin.y = 0
      },
      completion: {
        if $0 {
          dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(duration * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) {
            self.hide()
          }
        }
      }
    )
  }
  
  private func messageTextIsOneLine() -> Bool {
    let size = bottomLabel.attributedText.size()
    return size.width <= bottomLabel.frame.size.width
  }
  
  // MARK: Removing/Hiding the alert
  
  func hide(animated: Bool = true) {
    if animated {
      UIView.animateWithDuration(DropdownAppearance.Time.Animation,
        animations: {
          self.frame.origin.y = -DropdownAppearance.Height.Dropdown
        },
        completion: {
          if $0 {
            self.removeView()
          }
        }
      )
    } else {
      removeView()
    }
  }
  
  private func removeView() {
    self.removeFromSuperview()
  }
  
  // MARK: Touch up inside selector
  
  func didPressDropdown(sender: UIButton) {
    if delegate?.respondToNotification(self) ?? true {
      self.hide()
    }
  }
}

// MARK: - Dropdown delegate protocol definition

protocol MVDropdownAlertDelegate {
  func respondToNotification(notification: MVDropdownAlert) -> Bool
}

// MARK: - Dropdown alert object (holding all information about an alert)

struct MVDropdownAlertObject {
  let title: String
  let message: String?
  let style: MVDropdownAlertStyle
}

// MARK: - Dropdown style enumeration

enum MVDropdownAlertStyle {
  case Default
  case Danger
  case Warning
  case Info
}

// MARK: - Convenience initializer for dropdown alert object
extension MVDropdownAlertObject {
//  init(message: Message) {
//    
//  }
}
