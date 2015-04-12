//
//  InvitationCell.swift
//  myVerein
//
//  Created by Frank Steiler on 12/04/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import XCGLogger

class InvitationCell: UITableViewCell {

  let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var eventTitle: UILabel!
  @IBOutlet weak var eventLocation: UILabel!
  @IBOutlet weak var eventTime: UILabel!
  
  /// The delegate who gets notified if the user's response changes
  var delegate: InvitationCellDelegate?
  
  var event: Event! {
    didSet {
      if event != nil {
        eventTitle.text = event.title
        eventLocation.text = event.locationString
        eventTime.text = event.dateStringLong
      } else {
        logger.warning("Unable to populate cell because event could not be loaded")
      }
    }
  }
  
  /// This function is executed if the segemented control's value changes and notifies the delegate
  @IBAction func responseChanged(sender: UISegmentedControl) {
    logger.debug("Notifying delegate because control changed")
    if let delegate = delegate {
      switch sender.selectedSegmentIndex {
        case 0:
          logger.debug("Selected 'going' as response for \(event)")
          delegate.userRespondedTo(event: event, with: .Going)
        case 1:
          logger.debug("Selected 'maybe' as response for \(event)")
          delegate.userRespondedTo(event: event, with: .Maybe)
        case 2:
          logger.debug("Selected 'decline' as response for \(event)")
          delegate.userRespondedTo(event: event, with: .Decline)
        default:
          logger.error("Selected unrecognized state for \(event)")
      }
    } else {
      logger.warning("Unable to notify delegate, because delegate could not be unwrapped")
    }
  }

}

// MARK: - InvitationCellDelegate used to notify the delegate about a response to an invitation
protocol InvitationCellDelegate {
  func userRespondedTo(#event: Event, with response: EventResponse) -> ()
}
