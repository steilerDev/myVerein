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
//  MVColor.swift
//  This file is storing the complete color theme of this application as well as providing a convenience initializer for UIColor, handling hex codes.
//  On top of that the file is providing a method to match the button color to the application's color theme.
//

import UIKit

struct MVColor {
  struct Primary {
    static let Normal = 0x13CD78
    static let Light = 0x3AD18C
    static let Lighter = 0x66DAA5
    static let Dark = 0x00BC67
    static let Darker = 0x00904F
  }
  
  struct Gray {
    static let Normal = 0x555555
    static let Light = 0x999999
    static let Lighter = 0xEEEEEE
    static let Dark = 0x333333
    static let Darker = 0x222222
  }
  
  struct Info {
    static let Normal = 0x1D7FC4
    static let Light = 0x4191C9
    static let Lighter = 0x6AA8D4
    static let Dark = 0x076BB1
    static let Darker = 0x055085
  }
  
  struct Warning {
    static let Normal = 0xFF9A17
    static let Light = 0xFFAF47
    static let Lighter = 0xFFC477
    static let Dark = 0xFF9000
    static let Darker = 0xD07600
  }
  
  struct Danger {
    static let Normal = 0xFF5617
    static let Light = 0xFF7947
    static let Lighter = 0xFF9C77
    static let Dark = 0xFF4500
    static let Darker = 0xD03800
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

extension UIButton {
  func setToDefaultColor() {
    setTitleColor(UIColor(hex: MVColor.Primary.Normal), forState: .Normal)
    setTitleColor(UIColor(hex: MVColor.Primary.Normal), forState: .Selected)
    setTitleColor(UIColor(hex: MVColor.Primary.Darker), forState: .Highlighted)
    setTitleColor(UIColor(hex: MVColor.Gray.Light), forState: .Disabled)
  }
}
