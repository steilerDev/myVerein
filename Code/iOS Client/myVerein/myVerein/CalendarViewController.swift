//
//  CalendarViewController.swift
//  myVerein
//
//  Created by Frank Steiler on 28/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import JTCalendar
import XCGLogger

class CalendarViewController: UIViewController {

  let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var menuView: JTCalendarMenuView!
  
  @IBOutlet weak var contentView: JTCalendarContentView!
  
  var calendar = JTCalendar.new()
  
    override func viewDidLoad() {
      super.viewDidLoad()
      
      // All modifications on calendarAppearance have to be done before setMenuMonthsView and setContentView
      // Or you will have to call reloadAppearance
      calendar.calendarAppearance.calendar().firstWeekday = 2
      calendar.calendarAppearance.focusSelectedDayChangeMode = true
      
      calendar.menuMonthsView = menuView
      calendar.contentView = contentView
      calendar.dataSource = self
      
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}

extension CalendarViewController: JTCalendarDataSource {
  
  func calendarHaveEvent(calendar: JTCalendar!, date: NSDate!) -> Bool {
    return true
  }
  
  func calendarDidDateSelected(calendar: JTCalendar!, date: NSDate!) {
    logger.debug("Selected date \(date)")
  }
  
  func calendarDidLoadNextPage() {
    logger.debug("Did load next page")
  }
  
  func calendarDidLoadPreviousPage() {
    logger.debug("Did load previous page")
  }
}
