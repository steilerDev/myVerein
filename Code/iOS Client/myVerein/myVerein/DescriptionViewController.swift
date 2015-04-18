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
//  DescriptionViewController.swift
//  This file holds all information related to the description view.
//

import UIKit
import XCGLogger

class DescriptionViewController: UIViewController {

  let logger = XCGLogger.defaultInstance()
  
  var event: Event?
  
  @IBOutlet weak var descriptionTextView: UITextView!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    if let event = event, eventDescription = event.eventDescription where !eventDescription.isEmpty {
      descriptionTextView.text = event.eventDescription
      // TODO: Set title
    } else {
      logger.severe("Unable to load description view because event is nil")
      parentViewController?.dismissViewControllerAnimated(true, completion: nil)
    }
  }
}
