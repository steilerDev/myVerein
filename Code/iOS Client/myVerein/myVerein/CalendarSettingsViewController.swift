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

class CalendarSettingsViewController: UITableViewController {

  let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var notificationToggleCell: ToggleCell!
  
  @IBOutlet weak var notificationTimeCell: UITableViewCell!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    logger.debug("Calendar settings view loaded, populating now")
    notificationToggleCell.delegate = self
    notificationsSettingsCellChanged()
    setDetailText()
  }
  
  func notificationsSettingsCellChanged() {
    let notification = Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsEnabled].bool ?? true
    
    notificationToggleCell.toggleState = notification
    notificationTimeCell.userInteractionEnabled = notification
    notificationTimeCell.textLabel?.enabled = notification
    notificationTimeCell.detailTextLabel?.enabled = notification
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
    let alert = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
    let cancelAction = UIAlertAction(title: "Cancel", style: .Cancel, handler: nil)
    let atTimeOfEvent = UIAlertAction(title: "At time of event", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 0.0
      self.setDetailText()
    }
    let fiveMinutesBefore = UIAlertAction(title: "5 minutes before", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 300.0
      self.setDetailText()
    }
    let fifteenMinutesBefore = UIAlertAction(title: "15 minutes before", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 900.0
      self.setDetailText()
    }
    let thirtyMinutesBefore = UIAlertAction(title: "30 minutes before", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 1800.0
      self.setDetailText()
    }
    let oneHourBefore = UIAlertAction(title: "1 hour before", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 3600.0
      self.setDetailText()
    }
    let twoHoursBefore = UIAlertAction(title: "2 hours before", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 7200.0
      self.setDetailText()
    }
    let oneDayBefore = UIAlertAction(title: "1 day before", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 86400.0
      self.setDetailText()
    }
    let twoDaysBefore = UIAlertAction(title: "2 days before", style: .Default) {
      _ in
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 172800.0
      self.setDetailText()
    }
    alert.addAction(fiveMinutesBefore)
    alert.addAction(fifteenMinutesBefore)
    alert.addAction(thirtyMinutesBefore)
    alert.addAction(oneHourBefore)
    alert.addAction(twoHoursBefore)
    alert.addAction(oneDayBefore)
    alert.addAction(cancelAction)
    self.presentViewController(alert, animated: true, completion: nil)
  }

  func setDetailText() {
    let timeInterval = Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime].double ?? 1800.0
    switch timeInterval {
      case 0.0:
        notificationTimeCell.detailTextLabel?.text = "At time of event"
      case 300.0:
        notificationTimeCell.detailTextLabel?.text = "5 minutes before"
      case 900.0:
        notificationTimeCell.detailTextLabel?.text = "15 minutes before"
      case 1800.0:
        notificationTimeCell.detailTextLabel?.text = "30 minutes before"
      case 3600.0:
        notificationTimeCell.detailTextLabel?.text = "1 hour before"
      case 7200.0:
        notificationTimeCell.detailTextLabel?.text = "2 hour before"
      case 86400.0:
        notificationTimeCell.detailTextLabel?.text = "1 day before"
      case 172800.0:
        notificationTimeCell.detailTextLabel?.text = "2 days before"
      default:
        logger.warning("Unrecognized value stored in user defaults, using default one")
        Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsTime] = 1800.0
        notificationTimeCell.detailTextLabel?.text = "30 minutes before"
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
      Defaults[MVUserDefaultsConstants.Settings.Calendar.LocalNotificationsEnabled] = sender.toggleState
      notificationsSettingsCellChanged()
    default:
      logger.error("A unknown cell changed it's state")
    }
  }
}