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
import XCGLogger
import SwiftyUserDefaults
import JSQSystemSoundPlayer

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
  let originalFrame: CGRect
  
  var callbackOnHide: (()->())?
  
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
    
    originalFrame = CGRectMake(frame.origin.x, frame.origin.y, frame.width, frame.height)
    
    super.init(frame: frame)
    
    self.addSubview(topLabel)
    self.addSubview(bottomLabel)
    
    let gesture = UIPanGestureRecognizer(target: self, action: "didPanView:")
    self.addGestureRecognizer(gesture)
    
    self.addTarget(self, action: "didPressDropdown:", forControlEvents: .TouchUpInside)
  }

  required init(coder aDecoder: NSCoder) {
    topLabel = UILabel(coder: aDecoder)
    bottomLabel = UILabel(coder: aDecoder)
    originalFrame = CGRectMake(0, 0, 0, 0)
    super.init(coder: aDecoder)
  }
  
  convenience init() {
    self.init(frame: CGRectMake(0, -DropdownAppearance.Height.Dropdown, UIScreen.mainScreen().bounds.size.width, DropdownAppearance.Height.Dropdown))
  }
  
  // MARK: Showing the alert
  
  class func showAlert(alertObject: MVDropdownAlertObject, executeCallbackOnHide callback: (()->())? = nil) {
    MVDropdownAlert().showAlert(alertObject, executeCallbackOnHide: callback)
  }
  
  func showAlert(alertObject: MVDropdownAlertObject, executeCallbackOnHide callback: (()->())? = nil) {
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
    
    showAlertWithTitle(alertObject.title, message: alertObject.message, backgroundColor: backgroundColor, textColor: textColor, vibration: alertObject.vibrate, sound: alertObject.playSound, executeCallbackOnHide: callback)
  }

  func showAlertWithTitle(title: String, message: String? = nil, backgroundColor: UIColor = DropdownAppearance.Color.Default.Background, textColor: UIColor = DropdownAppearance.Color.Default.Text, vibration: Bool = true, sound: Bool = false, andDuration duration: Double = DropdownAppearance.Time.Visible, executeCallbackOnHide callback: (()->())? = nil) {
    self.callbackOnHide = callback
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
    
    if vibration {
      JSQSystemSoundPlayer.sharedPlayer().playVibrateSound()
    }
    
    if sound {
      JSQSystemSoundPlayer.jsq_playMessageReceivedSound()
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
    callbackOnHide?()
  }
  
  // MARK: Touch up inside selector
  
  func didPressDropdown(sender: UIButton) {
    if delegate?.respondToNotification(self) ?? true {
      self.hide()
    }
  }
  
  // MARK: Gesture recognizer
  
  func didPanView(gestureRecognizer: UIPanGestureRecognizer) {
    if gestureRecognizer.state == .Ended {
      if gestureRecognizer.view!.frame.origin.y < 0 {
        self.hide(animated: true)
      }
    } else {
      let translation = gestureRecognizer.translationInView(self.superview!)
      if(gestureRecognizer.view!.frame.origin.y + translation.y <= 0) {
        gestureRecognizer.view!.frame.origin.y += translation.y
      }
      gestureRecognizer.setTranslation(CGPointZero, inView: self.superview!)
    }
  }
}

// MARK: - MVDropdownAlertCenter

class MVDropdownAlertCenter {
  
  static var instance: MVDropdownAlertCenter = {
    XCGLogger.info("Creating new alert center")
    return MVDropdownAlertCenter()
  }()
  
  private let logger = XCGLogger.defaultInstance()

  /// Within this array all notifications are stored, that could not be shown because the center is currently showing another notification. This queue is only storing notifications that are either 'danger' or 'warning'.
  private var alertQueue: [MVDropdownAlertObject]!
  private var alertQueueLock = NSLock()
  
  /// This function proceses a notification and tries to display it. If a notification is currently shown, the notification gets discarded if it is not of style 'danger' or 'warning'. Since the storing of the object might block execution, the function is dispatching to a background queue.
  func showNotification(notification: MVDropdownAlertObject?) {
    if let notification = notification {
      {
        self.alertQueueLock.lock()
        if self.alertQueue != nil {
          if(notification.style == .Warning || notification.style == .Danger) {
            self.logger.debug("A notification is currently shown, appending notification \(notification) to the queue")
            self.alertQueue.append(notification)
          } else {
            self.logger.debug("A notification is currently shown and importance of new notification is too low: \(notification)")
          }
          self.alertQueueLock.unlock()
        } else {
          self.alertQueue = [notification]
          self.alertQueueLock.unlock()
          self.processQueue()
        }
      }~>
    }
  }
  
  /// This function proceses the alert queue. If all items have been processed the queue gets cleared and the process stopped. This function should not be called from the main queue since it might block execution, while trying to acquire the lock for the queue.
  func processQueue() {
    alertQueueLock.lock()
    if alertQueue == nil || alertQueue.isEmpty {
      alertQueue = nil
      alertQueueLock.unlock()
    } else if !alertQueue.isEmpty {
      let alert = alertQueue.removeAtIndex(0)
      alertQueueLock.unlock()
      ~>{ MVDropdownAlert.showAlert(alert, executeCallbackOnHide: { self.processQueue~> }) }
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
  let vibrate: Bool
  let playSound: Bool
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
  init?(message: Message) {
    if let notificationsEnabled = Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsEnabled.Key].bool where notificationsEnabled {
      self.title = message.division.name
      self.message = "\(message.sender.firstName!): \(message.content)"
      self.style = .Default
      self.vibrate = Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsVibration.Key].bool ?? MVUserDefaultsConstants.Settings.Messages.InAppNotificationsVibration.DefaultValue
      self.playSound = Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsSound.Key].bool ?? MVUserDefaultsConstants.Settings.Messages.InAppNotificationsSound.DefaultValue
    } else {
      return nil
    }
  }
  
  init?(localNotification: UILocalNotification) {
    if let notificationsEnabled = Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsEnabled.Key].bool where notificationsEnabled {
      self.title = localNotification.alertTitle
      self.message = localNotification.alertBody
      self.style = .Default
      self.vibrate = Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsVibration.Key].bool ?? MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsVibration.DefaultValue
      self.playSound = Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsSound.Key].bool ?? MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsSound.DefaultValue
    } else {
      return nil
    }
  }
  
  init(title: String, message: String? = nil, style: MVDropdownAlertStyle = .Default, vibrate: Bool = true, playSound: Bool = false) {
    self.title = title
    self.message = message
    self.style = style
    self.vibrate = vibrate
    self.playSound = playSound
  }
}
