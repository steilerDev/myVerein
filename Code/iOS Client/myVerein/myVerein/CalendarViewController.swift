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

  lazy var notificationBadge: RKNotificationHub = RKNotificationHub(barButtonItem: self.openInvitations)
  
  lazy var notificationDelegate: NotificationCountDelegate? = { self.tabBarController as? NotificationCountDelegate }()
  
  // Outlets from the storyboard
  @IBOutlet weak var menuView: JTCalendarMenuView!
  @IBOutlet weak var contentView: JTCalendarContentView!
  @IBOutlet weak var eventTableView: UITableView!
  @IBOutlet weak var openInvitations: UIBarButtonItem!
  @IBOutlet weak var refreshButton: UIBarButtonItem!
  
  let logger = XCGLogger.defaultInstance()
  let calendar = JTCalendar.new()
  
  var eventsOfSelectedDate: [Event]?
  
  /// The token handed over by the notification subscription, stored to be able to release resources.
  var notificationObserverToken: NSObjectProtocol?
}

// MARK: - UIViewController lifecycle methods
extension CalendarViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    // Hiding title on back button for EventViewController (See http://justabeech.com/2014/02/24/empty-back-button-on-ios7/ for reference)
    let backButton = UIBarButtonItem(title: "", style: .Plain, target: nil, action: nil)
    navigationItem.backBarButtonItem = backButton
    
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
  }
  
  /// Positioning calendar view to the current month
  override func viewDidLayoutSubviews() {
    super.viewDidLayoutSubviews()
    calendar.repositionViews()
  }
  
  /// Within this function the notification observer subscribes to the notification system.
  override func viewDidAppear(animated: Bool) {
    super.viewDidAppear(animated)
    // This observer is monitoring all events. As soon as the notification without a sender is received the controller is starting to reload its view.
    logger.debug("Calendar view controller subscribed to notification system")
    notificationObserverToken = MVNotification.subscribeToCalendarSyncCompletedNotificationForEvent(nil) {
      notification in
      if notification.object == nil {
        self.startRefresh()
      }
    }
  }
  
  /// Within this function the notification observer un-subscribes from the notification system.
  override func viewWillDisappear(animated: Bool) {
    super.viewWillDisappear(animated)
    if let notificationObserverToken = notificationObserverToken {
      logger.debug("Calendar view controller un-subscribed from notification system")
      MVNotification.unSubscribeFromNotification(notificationObserverToken)
    }
  }
  
  /// Moving loading of calendar data to view will appear, in hope to be more responsive
  override func viewWillAppear(animated: Bool) {
    super.viewWillAppear(animated)
    logger.debug("CalendarView did appear, loading calendar data and syncing events")
    startRefresh()
  }
  
  // MARK: Navigation
  override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
    if let identifier = segue.identifier {
      switch identifier {
      case CalendarViewControllerConstants.SegueToEvent:
        logger.debug("Preparing segue to event detail view")
        if let senderEvent = sender as? Event,
          destinationViewController = segue.destinationViewController as? EventViewController
        {
          destinationViewController.event = senderEvent
        } else {
          logger.error("Unable to get sender event or destination view controller")
        }
      case CalendarViewControllerConstants.SegueToInvitedEvents:
        logger.debug("Preparing segue to open invitations list")
        if let destinationViewController = (segue.destinationViewController as? UINavigationController)?.topViewController as? InvitationViewController {
          destinationViewController.delegate = self
        } else {
          logger.error("Unable to get destination view controller")
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
      logger.error("Unable to perform segue to event because the tapped cell was not found")
    }
  }
}

// MARK: - Delegate methods for control elements
extension CalendarViewController {
  
  /// This function starts the refresh of the page. This includes syncing of events, refreshing calendar view and setting amount of invited divisions.
  @IBAction func startRefresh() {
    logger.info("Calendar refresh started")
    startAnimatingRefreshButton()
    // In general the networking task of syncing the user's events takes longer than the calendar's reloading of data, so the refresh button's animation will stop after the networking task finished
    MVNetworkingHelper.syncUserEvent({
      self.updateNotificationCountTo(EventRepository().countPendingEvents(), sender: self)
      self.notificationBadge.pop()
      self.calendar.reloadData()
      self.eventTableView.reloadData()
      self.stopAnimatingRefreshButton()
    })
  }
  
  /// This function starts the animation of the refresh UIBarButtonItem. It uses keyframes to spin it until the animation is stopped by calling 'stopAnimationUIBarButtonItem:'.
  func startAnimatingRefreshButton() {
    UIView.animateKeyframesWithDuration(2.0,
      delay: 0.0,
      options: .CalculationModePaced | .Repeat,
      animations: {
        //The animation needs to be splitted in three parts, because otherwise the system does not know hot to apply the transformation matrix
        let buttonItemView = self.refreshButton.valueForKey("view") as! UIView // The animated view
        UIView.addKeyframeWithRelativeStartTime(0,
          relativeDuration: 1/3,
          animations: {
            buttonItemView.transform = CGAffineTransformMakeRotation(CGFloat(M_PI * 2/3)) // A third of the full rotation
          }
        )
        UIView.addKeyframeWithRelativeStartTime(1/3,
          relativeDuration: 1/3,
          animations: {
            buttonItemView.transform = CGAffineTransformMakeRotation(CGFloat(M_PI * 4/3)) // Two thirds of the full rotation
          }
        )
        UIView.addKeyframeWithRelativeStartTime(2/3,
          relativeDuration: 1/3,
          animations: {
            buttonItemView.transform = CGAffineTransformMakeRotation(CGFloat(M_PI * 2)) // Full rotation
          }
        )
      },
      completion: nil
    )
  }
  
  /// This function stops the animation of the refresh button and resets his position to its initial value.
  func stopAnimatingRefreshButton() {
    let buttonItemView = refreshButton.valueForKey("view") as! UIView // The animated view
    buttonItemView.layer.removeAllAnimations()
  }
  
  /// This function is called by pressing on the invitations icon and segues to the list of events.
  @IBAction func openInvitationsPressed() {
    logger.debug("Open invitations pressed, performing segue to modal view")
    performSegueWithIdentifier(CalendarViewControllerConstants.SegueToInvitedEvents, sender: openInvitations)
  }
}

// MARK: - NotificationCountDelegate protocol methods
extension CalendarViewController: NotificationCountDelegate {
  
  func decrementNotificationCountBy(amount: Int, sender: AnyObject?) {
    incrementNotificationCountBy(-amount, sender: sender)
  }
  
  func incrementNotificationCountBy(amount: Int, sender: AnyObject?) {
    if let notificationDelegate = notificationDelegate {
      notificationDelegate.incrementNotificationCountBy(amount, sender: self)
    }
    notificationBadge.incrementBy(Int32(amount))
    notificationBadge.pop()
  }
  
  func updateNotificationCountTo(newCount: Int, sender: AnyObject?) {
    if let notificationDelegate = notificationDelegate {
      notificationDelegate.updateNotificationCountTo(newCount, sender: self)
    }
    notificationBadge.setCount(Int32(newCount))
    notificationBadge.pop()
  }
}

// MARK: - CalendarViewController related constants
struct CalendarViewControllerConstants {
  static let BatchSize = 20
  static let Entity = EventConstants.ClassName
  static let PredicateField = EventConstants.Fields.StartDate
  static let SortField = EventConstants.Fields.StartDate
  static let ReuseCellIdentifier = "eventCell"
  static let SegueToEvent = "showEventDetailsFromCalendarView"
  static let SegueToInvitedEvents = "showInvitedEvents"
}

// MARK: - Extension for notification hub to support bar button item
extension RKNotificationHub {
  convenience init(barButtonItem: UIBarButtonItem) {
    self.init(view: barButtonItem.valueForKey("view") as! UIView)
    scaleCircleSizeBy(0.7)
    moveCircleByX(-5, y: 0)
  }
}
