//
//  MessagesSettingsViewController.swift
//  myVerein
//
//  Created by Frank Steiler on 20/04/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import XCGLogger
import SwiftyUserDefaults

class MessagesSettingsViewController: UITableViewController {

  let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var notificationsCell: ToggleCell!
  @IBOutlet weak var vibrationCell: ToggleCell!
  @IBOutlet weak var soundCell: ToggleCell!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    logger.debug("Messages settings controller did load, reading user defaults and setting view properties")
    notificationsCell.delegate = self
    notificationsCell.toggleState = Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsEnabled.Key].bool ?? MVUserDefaultsConstants.Settings.Messages.InAppNotificationsEnabled.DefaultValue
    
    vibrationCell.delegate = self
    vibrationCell.toggleState = Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsVibration.Key].bool ?? MVUserDefaultsConstants.Settings.Messages.InAppNotificationsVibration.DefaultValue
    
    soundCell.delegate = self
    soundCell.toggleState = Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsSound.Key].bool ?? MVUserDefaultsConstants.Settings.Messages.InAppNotificationsSound.DefaultValue
    
    notificationsSettingsCellChanged()
  }
  
  /// If the general settings toggle changed, this affects the other two toggles (If in app notifications are turned off, both vibration and sound are turned off as well)
  func notificationsSettingsCellChanged() {
    logger.info("General notifications cell was toggled")
    if !(Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsEnabled.Key].bool ?? MVUserDefaultsConstants.Settings.Messages.InAppNotificationsEnabled.DefaultValue) {
      Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsVibration.Key] = false
      vibrationCell.toggleState = false
      vibrationCell.enabled = false
      Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsSound.Key] = false
      soundCell.toggleState = false
      soundCell.enabled = false
    } else {
      vibrationCell.enabled = true
      soundCell.enabled = true
    }
  }

}

// MARK: - ToggleCell delegate
extension MessagesSettingsViewController: ToggleCellDelegate {
  func didChangeState(sender: ToggleCell) {
    logger.debug("A state change of a cell was recognized")
    switch sender {
      case notificationsCell:
        logger.info("Notification settings changed to \(sender.toggleState)")
        Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsEnabled.Key] = sender.toggleState
        notificationsSettingsCellChanged()
      case vibrationCell:
        logger.info("Vibration settings changed to \(sender.toggleState)")
        Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsVibration.Key] = sender.toggleState
      case soundCell:
        logger.info("Sound settings changed to \(sender.toggleState)")
        Defaults[MVUserDefaultsConstants.Settings.Messages.InAppNotificationsSound.Key] = sender.toggleState
      default:
        logger.error("A unknown cell changed it's state")
    }
  }
}
