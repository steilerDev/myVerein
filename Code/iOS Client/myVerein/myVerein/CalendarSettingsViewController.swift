//
//  CalendarSettingsViewController.swift
//  myVerein
//
//  Created by Frank Steiler on 20/04/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import XCGLogger
import SwiftyUserDefaults
import CoreData

class CalendarSettingsViewController: UITableViewController {

  let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var notificationToggleCell: ToggleCell!
  @IBOutlet weak var inAppNotificationToggleCell: ToggleCell!
  @IBOutlet weak var inAppNotificationVibrationToggleCell: ToggleCell!
  @IBOutlet weak var inAppNotificationSoundToggleCell: ToggleCell!
  
  @IBOutlet weak var notificationTimeCell: UITableViewCell!
  
  let initialDefaultNotificationTime = Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.Key].double ?? MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.DefaultValue
  
  override func viewDidLoad() {
    super.viewDidLoad()
    logger.debug("Calendar settings view loaded, populating now")
    notificationToggleCell.delegate = self
    inAppNotificationToggleCell.delegate = self
    inAppNotificationVibrationToggleCell.delegate = self
    inAppNotificationSoundToggleCell.delegate = self
    notificationsSettingsCellChanged()
    inAppNotificationsSettingsChanged()
    updateDetailText()
  }
  
  override func viewDidDisappear(animated: Bool) {
    super.viewDidDisappear(animated)
    // Checking if notifications settings have changed and applying them
    if Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsEnabled.Key].bool ?? MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsEnabled.DefaultValue {
      if let newDefaultNotificationTime = Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.Key].double where initialDefaultNotificationTime != newDefaultNotificationTime {
        logger.info("The default notification time changed, rescheduling notifications");
        
        
        {
          (context: NSManagedObjectContext) in
          let eventRepository = EventRepository(inContext: context)
          let eventsWithoutCustomReminder = eventRepository.findEventsWithoutCustomReminder()
          if let eventsWithoutCustomReminder = eventsWithoutCustomReminder {
            XCGLogger.debug("Found \(eventsWithoutCustomReminder.count) events without custom reminder")
            for event in eventsWithoutCustomReminder {
              event.scheduleNotification()
            }
          } else {
            XCGLogger.warning("Unable to find any events without custom reminder")
          }
        }~>
      }
    } else {
      logger.info("Notifications got disabled, cancelling scheduled notification")
      UIApplication.sharedApplication().cancelAllLocalNotifications()
    }
  }
  
  /// If the main notification toggle changed, the other cells need to be updated as well
  func notificationsSettingsCellChanged() {
    let notification = Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsEnabled.Key].bool ?? MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsEnabled.DefaultValue
    
    notificationToggleCell.toggleState = notification
    notificationTimeCell.userInteractionEnabled = notification
    notificationTimeCell.textLabel?.enabled = notification
    notificationTimeCell.detailTextLabel?.enabled = notification
    
    inAppNotificationToggleCell.enabled = notification
    
    if !notification {
      inAppNotificationToggleCell.toggleState = false
      Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsEnabled.Key] = false
      inAppNotificationsSettingsChanged()
    }
  }
  
  /// If the main in app notification toggle cahnged, the other cells might need to be updated as well
  func inAppNotificationsSettingsChanged() {
    let notification = Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsEnabled.Key].bool ?? MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsEnabled.DefaultValue
    
    inAppNotificationSoundToggleCell.enabled = notification
    inAppNotificationVibrationToggleCell.enabled = notification
    
    if !notification {
      inAppNotificationSoundToggleCell.toggleState = false
      Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsSound.Key] = false
      inAppNotificationVibrationToggleCell.toggleState = false
      Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsVibration.Key] = false
    }
  }
  
  override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
    if indexPath.section == 0 {
      logger.debug("Selected first section")
      switch indexPath.indexAtPosition(1) {
        case 0:
          logger.debug("Selected general settings toggle cell")
        case 1:
          logger.debug("Selected time cell")
          selectTime()
          notificationTimeCell.selected = false
        default:
          logger.warning("User selected unrecognized row in section 0")
      }
    } else {
      logger.warning("User selected unrecognized section")
    }
  }
  
  func selectTime() {
    let alert = TimeIntervalPickerAlertView(inViewController: self)
    alert.presentView()
  }

  func updateDetailText() {
    let timeInterval = Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.Key].double ?? MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.DefaultValue
    if let timeIntervalString = TimeIntervalPickerAlertView.intervalStringBefore(timeInterval) {
      notificationTimeCell.detailTextLabel?.text = timeIntervalString
    } else {
      logger.warning("Unrecognized value stored for notificaiton time interval in user defaults, using default one")
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.Key] = MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.DefaultValue
      notificationTimeCell.detailTextLabel?.text = TimeIntervalPickerAlertView.intervalStringBefore(MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.DefaultValue)
    }
  }
}

// MARK: - ToggleCell delegate
extension CalendarSettingsViewController: ToggleCellDelegate {
  func didChangeState(sender: ToggleCell) {
    logger.debug("A state change of a cell was recognized")
    switch sender {
    case notificationToggleCell:
      logger.info("Notification settings changed to \(sender.toggleState)")
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsEnabled.Key] = sender.toggleState
      notificationsSettingsCellChanged()
    case inAppNotificationToggleCell:
      logger.info("In app notification settings changed to \(sender.toggleState)")
      Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsEnabled.Key] = sender.toggleState
      inAppNotificationsSettingsChanged()
    case inAppNotificationSoundToggleCell:
      logger.info("In app notification sound settings changed to \(sender.toggleState)")
      Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsSound.Key] = sender.toggleState
    case inAppNotificationVibrationToggleCell:
      logger.info("In app notification vibration settings changed to \(sender.toggleState)")
      Defaults[MVUserDefaultsConstants.Settings.Calendar.InAppNotificationsVibration.Key] = sender.toggleState
    default:
      logger.error("A unknown cell changed it's state")
    }
  }
}

// MARK: - TimeIntervalPickerAlertView delegate
extension CalendarSettingsViewController: TimeIntervalPickerAlertViewDelegate {
  func userDidSelectAction(action: Double?) {
    if let action = action {
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime.Key] = action
      updateDetailText()
    }
  }
}