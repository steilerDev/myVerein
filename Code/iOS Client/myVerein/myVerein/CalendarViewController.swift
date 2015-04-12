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
//  CalendarViewController.swift
//  This file holds all information related to the calendar view, including appearance modifications, data source for the calendar and table view and delegate methods for click events.
//

import UIKit
import JTCalendar
import XCGLogger
import CoreData
import RKNotificationHub

// MARK: - All variables and outlets needed by the controller
class CalendarViewController: UIViewController {

  var notificationBadge: RKNotificationHub!
  
  // Outlets from the storyboard
  @IBOutlet weak var menuView: JTCalendarMenuView!
  @IBOutlet weak var contentView: JTCalendarContentView!
  @IBOutlet weak var eventTableView: UITableView!
  @IBOutlet weak var openInvitations: UIBarButtonItem!
  
  let logger = XCGLogger.defaultInstance()
  let calendar = JTCalendar.new()
  
  var eventsOfSelectedDate: [Event]?
}

// MARK: - UIViewController lifecycle methods
extension CalendarViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    // Hiding title on back button for EventViewController (See http://justabeech.com/2014/02/24/empty-back-button-on-ios7/ for reference)
    let backButton = UIBarButtonItem(title: "", style: .Plain, target: nil, action: nil)
    navigationItem.backBarButtonItem = backButton
    
    // Adding pull to refresh controll to table view
    let refreshControl = UIRefreshControl()
    refreshControl.addTarget(self, action: "startRefresh:", forControlEvents: .ValueChanged)
    self.eventTableView.addSubview(refreshControl)
    
    logger.debug("CalendarView did load, modifying the appearance")
    // All modifications on calendarAppearance have to be done before setMenuMonthsView and setContentView or you will have to call reloadAppearance
    calendar.calendarAppearance.calendar().firstWeekday = 2
    calendar.calendarAppearance.focusSelectedDayChangeMode = true
    calendar.calendarAppearance.dayCircleColorSelected = UIColor(hex: MVColor.Primary.Normal)
    calendar.calendarAppearance.dayCircleColorSelectedOtherMonth = UIColor(hex: MVColor.Primary.Normal)
    calendar.calendarAppearance.dayCircleColorToday = UIColor.whiteColor()
    calendar.calendarAppearance.dayCircleColorTodayOtherMonth = UIColor.whiteColor()
    calendar.calendarAppearance.dayTextColorToday = UIColor(hex: MVColor.Primary.Normal)
    calendar.calendarAppearance.dayTextColorTodayOtherMonth = UIColor(hex: MVColor.Primary.Normal)
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
    
    
    let imageView = UIImageView(image: openInvitations.image)
    notificationBadge = RKNotificationHub(view: imageView)
    notificationBadge.scaleCircleSizeBy(0.7)
    openInvitations.customView = imageView
  }
  
  override func viewWillAppear(animated: Bool) {
    super.viewWillAppear(animated)
    logger.debug("CalendarView will appear, syncing events")
    MVNetworkingHelper.syncUserEvent()
    //notificationBadge.incrementBy(3)
  }
  
  override func viewDidLayoutSubviews() {
    super.viewDidLayoutSubviews()
    calendar.repositionViews()
  }
  
  /// Moving loading of calendar data to view did appear, in hope to be more responsive
  override func viewDidAppear(animated: Bool) {
    super.viewDidAppear(animated)
    logger.debug("CalendarView did appear, loading calendar data")
    calendar.reloadData()
    
  }
  
  // MARK: Navigation
  
  // In a storyboard-based application, you will often want to do a little preparation before navigation
  override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
    if let identifier = segue.identifier {
      switch identifier {
      case CalendarViewControllerConstants.SegueToEvent:
        logger.debug("Preparing segue to event")
        if let senderEvent = sender as? Event,
          destinationViewController = segue.destinationViewController as? EventViewController
        {
          destinationViewController.event = senderEvent
        } else {
          logger.error("Unable to get sender event or destination view controller")
        }
      default: break;
      }
    } else {
      logger.error("Unable to get segue identifier")
    }
  }
}

// MARK: - JTCalendarDataSource protocol methods
extension CalendarViewController: JTCalendarDataSource {
  
  func calendarHaveEvent(calendar: JTCalendar!, date: NSDate!) -> Bool {
    return EventRepository().isEventOn(date: date)
  }
  
  func calendarDidDateSelected(calendar: JTCalendar!, date: NSDate!) {
    logger.debug("Selected date \(date), gathering events of the date and reloading table view")
    let eventRepository = EventRepository()
    eventsOfSelectedDate = eventRepository.findEventsBy(date: date)
    logger.debug("Retrieved events \(eventsOfSelectedDate)")
    eventTableView.reloadData()
  }
  
  func calendarDidLoadNextPage() {
    logger.debug("Did load next page")
  }
  
  func calendarDidLoadPreviousPage() {
    logger.debug("Did load previous page")
  }
}

// MARK: - UITableViewDataSource protocol methods
extension CalendarViewController: UITableViewDataSource {
  func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
    var cell = tableView.dequeueReusableCellWithIdentifier(CalendarViewControllerConstants.ReuseCellIdentifier, forIndexPath: indexPath) as! UITableViewCell
    if let eventsOfSelectedDate = eventsOfSelectedDate, event = eventsOfSelectedDate.get(indexPath.row) {
      logger.debug("Successfully fetched event \(event)")
      cell.textLabel?.text = event.title
      cell.detailTextLabel?.text = event.subTitle
    } else {
      logger.severe("Unable to fetch event at index path \(indexPath)")
      cell.textLabel?.text = ""
      cell.detailTextLabel?.text = ""
    }
    return cell
  }
  
  func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    logger.verbose("Gathering number of objects in section \(section)")
    if let eventsOfSelectedDate = eventsOfSelectedDate {
      return eventsOfSelectedDate.count
    } else {
      return 0
    }
  }
}

// MARK: - UITableViewDelegate protocol methods
extension CalendarViewController: UITableViewDelegate {
  func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
    tableView.cellForRowAtIndexPath(indexPath)?.selected = false
    if let selectedEvent = eventsOfSelectedDate?.get(indexPath.row) {
      logger.debug("Performing segue to event \(selectedEvent)")
      performSegueWithIdentifier(CalendarViewControllerConstants.SegueToEvent, sender: selectedEvent)
    } else {
      logger.error("Unable to perform segue to chat because the tapped cell was not found")
    }
  }
}

// MARK: - Delegate methods for control elements
extension CalendarViewController {
  func startRefresh(refreshControl: UIRefreshControl) {
    logger.info("Refresh started")
    MVNetworkingHelper.syncUserEvent()
    calendar.reloadData()
    refreshControl.endRefreshing()
  }
}

// MARK: - CalendarViewController related constants
struct CalendarViewControllerConstants {
  static let BatchSize = 20
  static let Entity = EventConstants.ClassName
  static let PredicateField = EventConstants.Fields.StartDate
  static let SortField = EventConstants.Fields.StartDate
  static let ReuseCellIdentifier = "eventCell"
  static let SegueToEvent = "showEventDetails"
}
