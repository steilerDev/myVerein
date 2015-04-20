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
//  SettingsViewController.swift
//  This file holds all information related to the settings view, including delegate methods for click events and segues to the designed view controller.
//

import UIKit
import XCGLogger

class SettingsViewController: UITableViewController {
  let logger = XCGLogger.defaultInstance()
}

// MARK: - UITableView delegate methods 
extension SettingsViewController {
  override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
    logger.verbose("Selected item object \(indexPath.indexAtPosition(1)) in section \(indexPath.indexAtPosition(0))")
    if let cell = tableView.cellForRowAtIndexPath(indexPath) {
      cell.selected = false
      switch indexPath.indexAtPosition(0) {
        case 0:
          logger.debug("Selected section 'system settings'")
          switch indexPath.indexAtPosition(1) {
            case 0:
              logger.debug("Selected item 'profile")
              performSegueWithIdentifier(SettingsViewControllerConstants.SegueTo.Profile, sender: cell)
            case 1:
              logger.debug("Selected item 'divisions'")
              performSegueWithIdentifier(SettingsViewControllerConstants.SegueTo.Division, sender: cell)
            default:
              logger.warning("Selected unknown item")
          }
        case 1:
          logger.info("Selected section 'application settings")
          switch indexPath.indexAtPosition(1) {
            case 0:
              logger.debug("Selected item 'messages'")
              performSegueWithIdentifier(SettingsViewControllerConstants.SegueTo.Messages, sender: cell)
            case 1:
              logger.debug("Selected item 'calendar'")
              performSegueWithIdentifier(SettingsViewControllerConstants.SegueTo.Calendar, sender: cell)
            default:
              logger.warning("Selected unknown item")
          }
        case 2:
          logger.debug("Selecte section 'log-out'")
          (UIApplication.sharedApplication().delegate as! AppDelegate).logoutUser()
        default:
          logger.warning("Selected unknown section")
      }
    } else {
      logger.warning("Unable to get selected cell")
    }
  }
}

// MARK: - Settings view related constants
struct SettingsViewControllerConstants {
  struct SegueTo {
    static let Profile = "showSystemProfileSettings"
    static let Division = "showSystemDivisionSettings"
    static let Calendar = "showApplicationCalendarSettings"
    static let Messages = "showApplicationMessagesSettings"
  }
}
