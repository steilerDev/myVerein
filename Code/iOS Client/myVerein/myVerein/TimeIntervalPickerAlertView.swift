//
//  TimeIntervalPickerAlertView.swift
//  myVerein
//
//  Created by Frank Steiler on 22/04/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit

class TimeIntervalPickerAlertView {
  
  /// This variable can be used to convert a time in seconds into a string, if the provided time is available. Dictionaries are unordered!
  private static let availableTimesString = [
    300.0: "5 minutes",
    900.0: "15 minutes",
    1800.0: "30 minutes",
    3600.0: "1 hour",
    7200.0: "2 hours",
    86400.0: "1 day",
    172800.0: "2 days"
  ]
  
  /// An array containing all valid times in seconds. This array is ordered!
  private static let availableTimes = [
    0.0,
    300.0,
    900.0,
    1800.0,
    3600.0,
    7200.0,
    86400.0,
    172800.0
  ]
  
  /// This function converts the specified interval into a string of the format "x minutes before"
  class func intervalStringBefore(interval: Double) -> String? {
    if interval == 0.0 {
      return "At time of event"
    } else if let timeString = availableTimesString[interval] {
      return "\(timeString) before"
    } else {
      return nil
    }
  }
  
  /// This function converts the specified interval into a string of the format "in x minutes"
  class func intervalStringIn(interval: Double) -> String? {
    if interval == 0.0 {
      return "now"
    } else if let timeString = availableTimesString[interval] {
      return "in \(timeString)"
    } else {
      return nil
    }
  }
  
  let alertViewController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
  let parentViewController: UIViewController
  
  init(inViewController viewController: UIViewController) {
    parentViewController = viewController
    
    // Setup of action sheet
    alertViewController.addAction(UIAlertAction(title: "Cancel", style: .Cancel, handler: { _ in self.userDidSelectAction(nil) }))
    for time in TimeIntervalPickerAlertView.availableTimes {
      if let timeString = TimeIntervalPickerAlertView.intervalStringBefore(time) {
        alertViewController.addAction(UIAlertAction(title: timeString, style: .Default, handler: { _ in self.userDidSelectAction(time)}))
      }
    }
  }
  
  func presentView() {
    // Presenting of action sheet
    parentViewController.presentViewController(alertViewController, animated: true, completion: nil)
  }
  
  func userDidSelectAction(action: Double?) {
    if let parentViewController = parentViewController as? TimeIntervalPickerAlertViewDelegate {
      parentViewController.userDidSelectAction(action)
    }
  }
}

protocol TimeIntervalPickerAlertViewDelegate {
  func userDidSelectAction(action: Double?)
}