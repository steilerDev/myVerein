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
import CoreData

class CalendarViewController: UIViewController {

  let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var menuView: JTCalendarMenuView!
  
  @IBOutlet weak var contentView: JTCalendarContentView!
  
  @IBOutlet weak var eventTableView: UITableView!
  
  let calendar = JTCalendar.new()
  
  struct CalendarViewControllerConstants {
    static let BatchSize = 20
    static let Entity = EventConstants.ClassName
    static let PredicateField = EventConstants.Fields.EndDate
    static let SortField = DivisionConstants.Fields.LatestMessage 
    static let CacheName = "myVerein.ChatOverviewCache"
    static let ReuseCellIdentifier = "ChatCell"
    static let SegueToChat = "showChatView"
  }
}

// MARK: - UIViewController lifecycle methods
extension CalendarViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    // All modifications on calendarAppearance have to be done before setMenuMonthsView and setContentView
    // Or you will have to call reloadAppearance
    calendar.calendarAppearance.calendar().firstWeekday = 2
    calendar.calendarAppearance.focusSelectedDayChangeMode = true
    calendar.calendarAppearance.dayCircleColorSelected = UIColor(hex: MVColor.Primary.Dark)
    calendar.calendarAppearance.dayCircleColorSelectedOtherMonth = UIColor(hex: MVColor.Primary.Dark)
    calendar.calendarAppearance.dayCircleColorToday = UIColor(hex: MVColor.Primary.Lighter)
    calendar.calendarAppearance.dayCircleColorTodayOtherMonth = UIColor(hex: MVColor.Primary.Lighter)
    calendar.calendarAppearance.dayDotColor = UIColor.blackColor()
    calendar.calendarAppearance.dayDotColorOtherMonth = UIColor.blackColor()
    calendar.calendarAppearance.monthBlock = {
      date, jt_calendar in
      let dateFormatter = NSDateFormatter()
      dateFormatter.dateFormat = "MMMM yyyy"
      return dateFormatter.stringFromDate(date)
    }
    
    calendar.dataSource = self
    calendar.menuMonthsView = menuView
    calendar.contentView = contentView
    
    calendar.reloadData()
  }
  
  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  
  override func viewWillAppear(animated: Bool) {
    logger.debug("CalendarView will appear, syncing events")
    MVNetworkingHelper.syncUserEvent()
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

// MARK: - JTCalendarDataSource protocol methods
extension CalendarViewController: JTCalendarDataSource {
  
  func calendarHaveEvent(calendar: JTCalendar!, date: NSDate!) -> Bool {
    return EventRepository().isEventOn(date: date)
  }
  
  func calendarDidDateSelected(calendar: JTCalendar!, date: NSDate!) {
    eventTableView.description
    logger.debug("Selected date \(date)")
  }
  
  func calendarDidLoadNextPage() {
    logger.debug("Did load next page")
  }
  
  func calendarDidLoadPreviousPage() {
    logger.debug("Did load previous page")
  }
}
