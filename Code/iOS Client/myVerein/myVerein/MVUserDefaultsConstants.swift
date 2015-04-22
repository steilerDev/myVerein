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
//  MVUserDefaultsConstants.swift
//  This file is storing the constants used to store and retrieve the user defaults.
//

import Foundation

struct MVUserDefaultsConstants {
  static let UserID = "userId"
  static let SystemID = "systemId"
  static let DeviceToken = "deviceToken"
  struct LastSynced {
    static let Event = "eventLastSynced"
  }
  
  struct Settings {
    struct Messages {
      struct InAppNotificationsEnabled {
        static let Key = "messagesInAppNotificationsEnabled"
        static let DefaultValue = true
      }
      struct InAppNotificationsVibration {
        static let Key = "messagesInAppNotificationsVibration"
        static let DefaultValue = true
      }
      struct InAppNotificationsSound {
        static let Key = "messagesInAppNotficationsSound"
        static let DefaultValue = true
      }
    }
    struct Calendar {
      struct LocalNotificationsEnabled {
        static let Key = "calendarLocalNotificationsEnabled"
        static let DefaultValue = true
      }
      struct LocalNotificationsTime {
        static let Key = "calendarLocalNotificationsTime"
        static let DefaultValue = 1800.0
      }
      struct InAppNotificationsEnabled {
        static let Key = "calendarInAppNotificationsEnabled"
        static let DefaultValue = true
      }
      struct InAppNotificationsVibration {
        static let Key = "calendarInAppNotificaionsVibration"
        static let DefaultValue = true
      }
      struct InAppNotificationsSound {
        static let Key = "calendarInAppNotficationsSound"
        static let DefaultValue = true
      }
    }
  }
}
