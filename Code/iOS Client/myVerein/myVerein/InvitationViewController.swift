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
//  InvitationViewController.swift
//  This file holds all information related to the invitation view, including appearance modifications, data source for the table view and delegate methods for click events.
//

import UIKit
import CoreData
import XCGLogger

class InvitationViewController: UITableViewController {

  /// The delegate for this view controller, which gets notified if the amount of open invitation changed
  var delegate: InvitationViewDelegate?
  
  let logger = XCGLogger.defaultInstance()
  
  /// This variable stores the new responses of a user in a map where the key is the eventID and the value is the selected response
  var newResponses = [Event: EventResponse]()
  
  /// Lazily initiated fetched result controller for controller
  lazy var fetchedResultController: NSFetchedResultsController = {
    
    let fetchRequest = NSFetchRequest(entityName: InvitationViewControllerConstants.Entity)
    fetchRequest.fetchBatchSize = InvitationViewControllerConstants.BatchSize
    
    let sortDescriptor = NSSortDescriptor(key: InvitationViewControllerConstants.SortField, ascending: true)
    fetchRequest.sortDescriptors = [sortDescriptor]
    
    let predicate = NSPredicate(format: "\(EventConstants.RawFields.UserResponse) == %@", EventResponse.Pending.rawValue)
    fetchRequest.predicate = predicate
    
    //Initializing data source (NSFetchedResultController)
    let controller = NSFetchedResultsController(fetchRequest: fetchRequest,
      managedObjectContext: (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!,
      sectionNameKeyPath: nil,
      cacheName: InvitationViewControllerConstants.CacheName
    )
    
    controller.delegate = self
    
    return controller
  }()
  
}

// MARK: - UIViewController lifecycle methods
extension InvitationViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    // Hiding title on back button for EventViewController (See http://justabeech.com/2014/02/24/empty-back-button-on-ios7/ for reference)
    let backButton = UIBarButtonItem(title: "", style: .Plain, target: nil, action: nil)
    navigationItem.backBarButtonItem = backButton
    
    // Accessing fetched result controller and therfore initiating it if it did not happen yet
    var error: NSError? = nil
    if fetchedResultController.performFetch(&error) {
      logger.info("Successfully initiated invitation view data source")
    } else {
      logger.error("Unable to initiate invitation view data source: \(error?.description)")
    }
  }
  
  
  // MARK:  Navigation
  
  // In a storyboard-based application, you will often want to do a little preparation before navigation
  override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
    if let identifier = segue.identifier {
      switch identifier {
        case InvitationViewControllerConstants.SegueToEvent:
          logger.debug("Preparing segue to event detail view")
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

// MARK: - UITableView data source functions
extension InvitationViewController {
  override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
    return fetchedResultController.sections?.count ?? 1
  }
  
  override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    logger.verbose("Gathering number of rows in section \(section)")
    if let sections = fetchedResultController.sections where sections.count > 0 {
      if let sectionInfo = sections[section] as? NSFetchedResultsSectionInfo {
        let objectCount = sectionInfo.numberOfObjects
        logger.debug("Successfully gathered number of objects in section \(section): \(objectCount)")
        return objectCount
      } else {
        logger.warning("Unable to gather number of objects in section \(section), because section info can't be retrieved")
        return 0
      }
    } else {
      logger.warning("Unable to gather number of objects in section \(section), because the sections can't be retrieved")
      return 0
    }
  }
  
  override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> InvitationCell {
    let cell = tableView.dequeueReusableCellWithIdentifier(InvitationViewControllerConstants.ReuseCellIdentifier, forIndexPath: indexPath) as! InvitationCell
    return configureCell(cell, atIndexPath: indexPath)
  }
  
  /// This function re-configures the cell at the provided index path
  func configureCell(atIndexPath indexPath: NSIndexPath) -> InvitationCell {
    let cell = tableView?.cellForRowAtIndexPath(indexPath) as! InvitationCell
    return configureCell(cell, atIndexPath: indexPath)
  }
  
  /// This function configures the cell at the provided index path
  func configureCell(cell: InvitationCell, atIndexPath indexPath: NSIndexPath) -> InvitationCell {
    if let event = fetchedResultController.objectAtIndexPath(indexPath) as? Event {
      cell.event = event
      cell.delegate = self
    } else {
      logger.warning("Unable to load event for index path \(indexPath)")
    }
    return cell
  }
}

// MARK: - UITableView delegate methods
extension InvitationViewController {
  override func tableView(tableView: UITableView, accessoryButtonTappedForRowWithIndexPath indexPath: NSIndexPath) {
    if let event = fetchedResultController.objectAtIndexPath(indexPath) as? Event {
      logger.debug("Performing segue to event details view")
      performSegueWithIdentifier(InvitationViewControllerConstants.SegueToEvent, sender: event)
    } else {
      logger.error("Unable to perform segue to event details view, because event at index path \(indexPath) could not be retrieved")
    }
  }
}

// MARK: - Button-click delegate methods
extension InvitationViewController {
  /// This function is called when the cancle button is pressed. The function dismisses the view controller without saving the events
  @IBAction func cancelButtonPressed() {
    presentingViewController?.dismissViewControllerAnimated(true, completion: nil)
  }
  
  /// This function is called when the done button is pressed. The function dismisses the view controller, saves the selected responses and sends them to the server.
  @IBAction func doneButtonPressed() {
    logger.info("Sending responses and saving events")
    let eventRepository = EventRepository()
    for (event, response) in newResponses {
      logger.debug("Sending \(response) for \(event)")
      event.response = response
      MVNetworkingHelper.sendEventResponse(event)
    }
    delegate?.decrementOpenInvitationCount(newResponses.count)
    cancelButtonPressed()
  }
}

// MARK: - InvitationCell delegate methods
extension InvitationViewController:  InvitationCellDelegate {
  func userRespondedTo(#event: Event, with response: EventResponse) {
    newResponses[event] = response
  }
}

// MARK: - NSFetchedResultsController delegate methods
extension InvitationViewController: NSFetchedResultsControllerDelegate {
  func controllerWillChangeContent(controller: NSFetchedResultsController) {
    tableView.beginUpdates()
  }
  
  func controller(controller: NSFetchedResultsController, didChangeSection sectionInfo: NSFetchedResultsSectionInfo, atIndex sectionIndex: Int, forChangeType type: NSFetchedResultsChangeType) {
    switch type {
    case .Insert:
      tableView.insertSections(NSIndexSet(index: sectionIndex), withRowAnimation: .Fade)
    case .Delete:
      tableView.deleteSections(NSIndexSet(index: sectionIndex), withRowAnimation: .Fade)
    default: break
    }
  }
  
  func controller(controller: NSFetchedResultsController, didChangeObject anObject: AnyObject, atIndexPath indexPath: NSIndexPath?, forChangeType type: NSFetchedResultsChangeType, newIndexPath: NSIndexPath?) {
    switch type {
    case .Insert:
      tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: .Fade)
    case .Delete:
      tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: .Fade)
    case .Update:
      configureCell(atIndexPath: indexPath!)
    case .Move:
      tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: .Fade)
      tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: .Fade)
    }
  }
  
  func controllerDidChangeContent(controller: NSFetchedResultsController) {
    tableView.endUpdates()
  }
}

// MARK: - InvitationViewDelegate protocol definition, enabling to update of the amount of unanswered invitations
protocol InvitationViewDelegate {
  /// This function is called after the view controller is dismissed. It tells the delegate how many invitations have received a response.
  func decrementOpenInvitationCount(decrement: Int) -> ()
}

// MARK: - InvitationViewController related constants
struct InvitationViewControllerConstants {
  static let Entity = EventConstants.ClassName
  static let BatchSize = 20
  static let SortField = EventConstants.Fields.StartDate
  static let CacheName = "InvitationViewControllerCache"
  static let ReuseCellIdentifier = "invitedEventCell"
  static let SegueToEvent = "showEventDetailsFromInvitationView"
}
