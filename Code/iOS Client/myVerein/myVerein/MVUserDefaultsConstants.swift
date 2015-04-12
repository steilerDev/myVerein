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
}
