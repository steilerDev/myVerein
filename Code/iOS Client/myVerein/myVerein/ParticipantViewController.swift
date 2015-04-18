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
//  ParticipantViewController.swift
//  This file holds all information related to the participan view, including user loading, delegate methods for click events and participant syncing.
//

import UIKit
import CoreData
import XCGLogger

class ParticipantViewController: UITableViewController {
  let logger = XCGLogger.defaultInstance()
  
  /// The event this participant view controller is refering to
  var event: Event!
  
  /// Lazily initiated pull to refresh control element with a delegate method 'refreshDidStart:' in this object
  lazy var refreshControlElement: UIRefreshControl = {
    let refreshControl = UIRefreshControl()
    refreshControl.addTarget(self, action: "refreshDidStart:", forControlEvents: .ValueChanged)
    return refreshControl
  }()
  
  /// Lazily initiated segmented control element with a delegate method 'segmentedControlDidChange:' in this object
  lazy var segmentedControlElement: UISegmentedControl = {
    let segmentedControl = UISegmentedControl(items: ["Going", "Maybe", "Declined", "Pending"])
    segmentedControl.addTarget(self, action: "segmentedControlDidChange:", forControlEvents: .ValueChanged)
    segmentedControl.sizeToFit()
    segmentedControl.selectedSegmentIndex = 0
    return segmentedControl
  }()
  
  /// Lazily initiated fetched result controller for controller
  lazy var fetchedResultController: NSFetchedResultsController = {
    
    let fetchRequest = NSFetchRequest(entityName: ParticipantViewControllerConstants.Entity)
    fetchRequest.fetchBatchSize = ParticipantViewControllerConstants.BatchSize
    
    let sortDescriptor = NSSortDescriptor(key: ParticipantViewControllerConstants.SortField, ascending: true)
    fetchRequest.sortDescriptors = [sortDescriptor]
    
    //Initializing data source (NSFetchedResultController), but not using cache, since the predicate changes fairly often
    var controller = NSFetchedResultsController(fetchRequest: fetchRequest,
      managedObjectContext: (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!,
      sectionNameKeyPath: nil,
      cacheName: nil
    )
    
    controller.delegate = self
    
    return controller
  }()
}

// MARK: - ViewController lifecycle methods
extension ParticipantViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    if event == nil {
      logger.error("Unable to load event for event participation view, dismissing view controller")
      navigationController?.popViewControllerAnimated(true)
    }
    
    // Adding refresh control
    tableView.addSubview(refreshControlElement)
    
    // Adding segmented control to title bar
    navigationItem.titleView = segmentedControlElement
    segmentedControlDidChange(segmentedControlElement)
  }
}

// MARK: - Delegate methods for control elements
extension ParticipantViewController {
  /// This function is called every time the user changes the control within the title bar. This function resets the data in the table view and loads the appropriate data from the database. At the same time it is kicking off the pull to refresh control and its behaviour (defined in the 'refreshDidStart:' function)
  func segmentedControlDidChange(control: UISegmentedControl) {
    logger.debug("UISegementedControl changed to control \(control.selectedSegmentIndex)")
    
    logger.info("Clearing table view data")
    tableView.dataSource = nil
    tableView.reloadData()
    
    refreshControlElement.beginRefreshing()
    switch control.selectedSegmentIndex {
      case 0:
        logger.info("Selected 'going' segment")
        fetchedResultController.fetchRequest.predicate = NSPredicate(format: "\(ParticipantViewControllerConstants.PredicateFields.Going) CONTAINS %@", event!)
      case 1:
        logger.info("Selected 'maybe' segement")
        fetchedResultController.fetchRequest.predicate = NSPredicate(format: "\(ParticipantViewControllerConstants.PredicateFields.Maybe) CONTAINS %@", event!)
      case 2:
        logger.info("Selected 'declined' segment")
        fetchedResultController.fetchRequest.predicate = NSPredicate(format: "\(ParticipantViewControllerConstants.PredicateFields.Declined) CONTAINS %@", event!)
      case 3:
        logger.info("Selected 'pending' segment")
        fetchedResultController.fetchRequest.predicate = NSPredicate(format: "\(ParticipantViewControllerConstants.PredicateFields.Pending) CONTAINS %@", event!)
      default:
        logger.warning("Selected unknown segment")
        return
    }
    
    // Performing fetch request with new predicate
    var error: NSError? = nil
    if fetchedResultController.performFetch(&error) {
      logger.info("Successfully performed fetch request for new participant predicate")
    } else {
      logger.error("Unable to performed fetch request for new participant predicate: \(error?.extendedDescription)")
    }
    tableView.dataSource = self
    tableView.reloadData()
    logger.info("Starting resync after change of selected segment")
    refreshControlElement.beginRefreshing()
    refreshDidStart(refreshControlElement)
  }
  
  /// This function is the delegate method for the pull to refresh control and is called as soon as the refresh started. The function is resyncing the participants for the currently selected event response.
  func refreshDidStart(refreshControl: UIRefreshControl) {
    logger.debug("Pull to refresh started")
    
    let response: EventResponse!
    switch segmentedControlElement.selectedSegmentIndex {
    case 0:
      logger.info("Selected 'going' segment")
      response = .Going
    case 1:
      logger.info("Selected 'maybe' segement")
      response = .Maybe
    case 2:
      logger.info("Selected 'declined' segment")
      response = .Decline
    case 3:
      logger.info("Selected 'pending' segment")
      response = .Pending
    default:
      logger.warning("Selected unknown segment")
      refreshControlElement.endRefreshing()
      return
    }
    
    logger.verbose("Getting user with \(response) responses for event \(self.event)")
    MVNetworking.defaultInstance().eventGetResponseAction(
      eventID: event.id,
      response: response,
      success: {
        responseObject in
        let logger = XCGLogger.defaultInstance()
        let userRepository = UserRepository()
        if let responseObject = responseObject as? [String] {
          let (user: [User]?, error) = userRepository.getOrCreateUsingArray(responseObject, AndSync: true)
          if error != nil && user == nil {
            logger.error("Unable to get participants, because response object could not be read: \(error!.extendedDescription)")
          } else {
            logger.info("Successfully parsed participants with \(response) response for \(self.event)")
            switch response! {
            case .Going:
              self.event.rawGoingUser = NSSet(array: user!)
            case .Pending:
              self.event.rawPendingUser = NSSet(array: user!)
            case .Maybe:
              self.event.rawMaybeUser = NSSet(array: user!)
            case .Decline:
              self.event.rawDeclinedUser = NSSet(array: user!)
            default:
              logger.error("Unable to assign new user to event set, because response was unrecognized")
            }
            userRepository.save()
          }
        } else {
          logger.error("Unable to get participants with \(response) response for \(self.event), because response object could not be read")
        }
        self.refreshControlElement.endRefreshing()
      },
      failure: {
        error in
        XCGLogger.error("Unable to get participants with \(response) response for \(self.event): \(error.extendedDescription)")
        self.refreshControlElement.endRefreshing()
      }
    )
  }
}

// MARK: - UITableView data source functions
extension ParticipantViewController {
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
  
  override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
    let cell = tableView.dequeueReusableCellWithIdentifier(ParticipantViewControllerConstants.ReuseCellIdentifier, forIndexPath: indexPath) as! UITableViewCell
    return configureCell(cell, atIndexPath: indexPath)
  }
  
  /// This function configures the cell at the provided index path
  func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) -> UITableViewCell {
    if let user = fetchedResultController.objectAtIndexPath(indexPath) as? User {
      cell.textLabel?.text = user.displayName
      if let userAvatar = user.avatar {
        cell.imageView?.image = userAvatar
      } else {
        //cell.imageView?.setImageWithString(user.displayName, color: UIColor(hex: MVColor.Primary.Normal))
      }
    } else {
      logger.warning("Unable to load user for index path \(indexPath)")
    }
    return cell
  }
}

// MARK: - NSFetchedResults controller delegate functions 
extension ParticipantViewController: NSFetchedResultsControllerDelegate {
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
        configureCell(tableView.cellForRowAtIndexPath(indexPath!)!, atIndexPath: indexPath!)
      case .Move:
        tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: .Fade)
        tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: .Fade)
    }
  }
  
  func controllerDidChangeContent(controller: NSFetchedResultsController) {
    tableView.endUpdates()
  }
}

// MARK: - ParticipantView related constants
struct ParticipantViewControllerConstants {
  static let Entity = UserConstants.ClassName
  static let BatchSize = 20
  static let SortField = UserConstants.Fields.FirstName
  static let ReuseCellIdentifier = "participantCell"
  struct PredicateFields {
    static let Going = UserConstants.RawFields.GoingEvents
    static let Maybe = UserConstants.RawFields.MaybeEvents
    static let Declined = UserConstants.RawFields.DeclinedEvents
    static let Pending = UserConstants.RawFields.PendingEvents
  }
}
