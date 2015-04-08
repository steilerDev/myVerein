//
//  DivisionChatOverview.swift
//  myVerein
//
//  Created by Frank Steiler on 03/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import CoreData
import XCGLogger

class DivisionChatOverviewViewController: UICollectionViewController {
  
  struct DivisionChatOverviewConstants {
    static let BatchSize = 20
    static let Entity = DivisionConstants.ClassName
    static let PredicateField = DivisionConstants.UserMembershipStatus
    static let SortField = DivisionConstants.LatestMessage + "." + MessageConstants.TimestampField
    static let CacheName = "myVerein.ChatOverviewCache"
    static let ReuseCellIdentifier = "ChatCell"
    static let SegueToChat = "showChatView"
  }
  
  let logger = XCGLogger.defaultInstance()
  
  // Lazily initiating fetched result controller
  lazy var fetchedResultController: NSFetchedResultsController = {
    //Initializing data source (NSFetchedResultController)
    let fetchRequest = NSFetchRequest(entityName: DivisionChatOverviewConstants.Entity)
    fetchRequest.fetchBatchSize = DivisionChatOverviewConstants.BatchSize
    
    /// TODO: Improve sort
    let sortDescriptor = NSSortDescriptor(key: DivisionChatOverviewConstants.SortField, ascending: false)
    fetchRequest.sortDescriptors = [sortDescriptor]
    
    let predicate = NSCompoundPredicate(type: .OrPredicateType,
      subpredicates: [
        NSPredicate(format: "\(DivisionChatOverviewConstants.PredicateField) == %@", Division.UserMembershipStatus.Member.rawValue),
        NSPredicate(format: "\(DivisionChatOverviewConstants.PredicateField) == %@", Division.UserMembershipStatus.FormerMember.rawValue)
      ]
    )
    fetchRequest.predicate = predicate
    var controller = NSFetchedResultsController(fetchRequest: fetchRequest,
      managedObjectContext: (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!,
      sectionNameKeyPath: nil,
      cacheName: DivisionChatOverviewConstants.CacheName
    )
    
    controller.delegate = self
    
    return controller
  }()
  
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    logger.verbose("Initiating division chat overview data source")
    
    //Syncing user divisions everytime the view loads
    MVNetworkingHelper.syncUserDivision()
    
    // Accessing fetched result controller and therfore initiating it if it did not happen yet
    var error: NSError? = nil
    if fetchedResultController.performFetch(&error) {
      logger.info("Successfully initiated division chat overview data source")
    } else {
      logger.error("Unable to initiate division chat overview data source: \(error?.localizedDescription)")
    }
    
    // Uncomment the following line to preserve selection between presentations
    // self.clearsSelectionOnViewWillAppear = false
  }
  
  // MARK: Navigation
  
  override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if let identifier = segue.identifier {
      switch identifier {
      case DivisionChatOverviewConstants.SegueToChat:
        logger.debug("Preparing segue to chat")
        if let senderCell = sender as? DivisionChatCell,
          senderDivision = senderCell.division,
          destinationViewController = segue.destinationViewController as? ChatViewController
        {
          destinationViewController.division = senderDivision
        } else {
          logger.error("Unable to determine sender of segue, sender division or destination view controller")
        }
      default: break;
      }
    } else {
      logger.error("Unable to get segue identifier")
    }
  }
  
  // MARK: NSFetchedResultsControllerDelegate (Needed variables)
  
  /// Dictionary used to collect section changes
  private lazy var sectionChanges: [NSFetchedResultsChangeType: NSMutableIndexSet] = {
    // Setting up objects in dict
    var dict = [NSFetchedResultsChangeType: NSMutableIndexSet]()
    dict[.Insert] = NSMutableIndexSet() as NSMutableIndexSet
    dict[.Delete] = NSMutableIndexSet() as NSMutableIndexSet
    return dict
    }()
  
  /// Dictionary used to collect object changes
  private lazy var objectChanges: [NSFetchedResultsChangeType: [NSIndexPath]] = {
    // Setting up objects in dict
    var dict = [NSFetchedResultsChangeType: [NSIndexPath]]()
    dict[.Insert] = [NSIndexPath]()
    dict[.Delete] = [NSIndexPath]()
    dict[.Update] = [NSIndexPath]()
    return dict
    }()
}

// MARK: - UICollectionViewDataSource
extension DivisionChatOverviewViewController: UICollectionViewDataSource {
  override func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
    return fetchedResultController.sections?.count ?? 1
  }
  
  override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    logger.verbose("Gathering number of objects in section \(section)")
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
  
  override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
    logger.verbose("Gathering cell for index path \(indexPath)")
    let cell = collectionView.dequeueReusableCellWithReuseIdentifier(DivisionChatOverviewConstants.ReuseCellIdentifier, forIndexPath: indexPath) as! DivisionChatCell
    
    // Configure the cell
    configureCell(indexPath, cell: cell)
    
    return cell
  }
  
  private func configureCell(indexPath: [NSIndexPath]) {
    for path in indexPath {
      if let currentCell = collectionView?.cellForItemAtIndexPath(path) as? DivisionChatCell {
        configureCell(path, cell: currentCell)
      } else {
        logger.error("Unable to get cell for item path \(path)")
      }
    }
  }
  
  private func configureCell(indexPath: NSIndexPath, cell: DivisionChatCell) {
    logger.debug("Configuring cell for index path \(indexPath)")
    cell.division = fetchedResultController.objectAtIndexPath(indexPath) as? Division
    cell.updateAvatarView()
  }
}

// MARK: - UICollectionViewDelegate
extension DivisionChatOverviewViewController: UICollectionViewDelegate {
  override func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
    if let selectedCell = collectionView.cellForItemAtIndexPath(indexPath) as? DivisionChatCell {
      performSegueWithIdentifier(DivisionChatOverviewConstants.SegueToChat, sender: selectedCell)
      selectedCell.notificationCount = 0
    } else {
      logger.error("Unable to perform segue to chat because the tapped cell was not found")
    }
  }
  
  /*
  // Uncomment this method to specify if the specified item should be highlighted during tracking
  override func collectionView(collectionView: UICollectionView, shouldHighlightItemAtIndexPath indexPath: NSIndexPath) -> Bool {
  return true
  }
  */
  
  /*
  // Uncomment these methods to specify if an action menu should be displayed for the specified item, and react to actions performed on the item
  override func collectionView(collectionView: UICollectionView, shouldShowMenuForItemAtIndexPath indexPath: NSIndexPath) -> Bool {
  return false
  }
  
  override func collectionView(collectionView: UICollectionView, canPerformAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) -> Bool {
  return false
  }
  
  override func collectionView(collectionView: UICollectionView, performAction action: Selector, forItemAtIndexPath indexPath: NSIndexPath, withSender sender: AnyObject?) {
  
  }
  */
}

// MARK: - NSFetchedResultsControllerDelegate
extension DivisionChatOverviewViewController: NSFetchedResultsControllerDelegate {
  /// Collecting section changes in dictionary and applying them in a batch after they have all been applied
  func controller(controller: NSFetchedResultsController, didChangeSection sectionInfo: NSFetchedResultsSectionInfo, atIndex sectionIndex: Int, forChangeType type: NSFetchedResultsChangeType) {
    switch type {
    case .Insert: fallthrough
    case .Delete:
      sectionChanges[type]?.addIndex(sectionIndex)
    default:
      break;
    }
  }
  
  /// Collecting object changes in dictionary and applying them in a batch after they have all been applied
  func controller(controller: NSFetchedResultsController, didChangeObject anObject: AnyObject, atIndexPath indexPath: NSIndexPath?, forChangeType type: NSFetchedResultsChangeType, newIndexPath: NSIndexPath?) {
    logger.debug("Object in division chat overview changed.")
    if let collectionView = self.collectionView {
      switch type {
      case .Insert:
        if let currentIndexPath = newIndexPath {
          objectChanges[type]?.append(currentIndexPath)
        } else {
          logger.error("Unable to insert object in chat overview")
        }
      case .Delete:
        if let currentIndexPath = indexPath {
          objectChanges[type]?.append(currentIndexPath)
        } else {
          logger.error("Unable to delete object in chat overview")
        }
      case .Update:
        if let currentIndexPath = indexPath {
          objectChanges[type]?.append(currentIndexPath)
        } else {
          logger.error("Unable to update object in chat overview")
        }
      case .Move:
        if let currentIndexPath = newIndexPath, oldIndexPath = indexPath {
          objectChanges[.Delete]?.append(oldIndexPath)
          objectChanges[.Insert]?.append(currentIndexPath)
        } else {
          logger.error("Unable to move object in chat overview")
        }
      }
    } else {
      logger.error("Unable to change object in collection view")
    }
  }
  
  /// Batch changing controller after the content finished changing. This needs to be done since CollectionView and NSFetchedResultsController don't play nicely. See https://github.com/ashfurrow/UICollectionView-NSFetchedResultsController
  func controllerDidChangeContent(controller: NSFetchedResultsController) {
    logger.debug("Applying batch changes because model changed")
    if let currentCollectionView = collectionView {
      // Apply section changes
      if (sectionChanges[.Insert]?.count > 0) || (sectionChanges[.Delete]?.count > 0) {
        logger.debug("Applying section changes")
        currentCollectionView.performBatchUpdates({
          for (type, section) in self.sectionChanges {
            switch type {
            case .Insert:
              currentCollectionView.insertSections(section)
            case .Delete:
              currentCollectionView.deleteSections(section)
            default:
              XCGLogger.debugExec {abort()}
              XCGLogger.error("Reached default case while applying section changes. This should not happen: Type \(type.rawValue)")
            }
          }
          }, completion: nil)
      } else if !(objectChanges[.Insert]?.isEmpty ?? true) || !(objectChanges[.Delete]?.isEmpty ?? true) || !(objectChanges[.Update]?.isEmpty ?? true) { //Applying object changes only if there was no section changes
        logger.debug("Applying object changes")
        currentCollectionView.performBatchUpdates({
          for (type, indexPath) in self.objectChanges {
            switch type {
            case .Insert:
              currentCollectionView.insertItemsAtIndexPaths(indexPath)
            case .Delete:
              currentCollectionView.deleteItemsAtIndexPaths(indexPath)
            case .Update:
              self.configureCell(indexPath)
            default:
              XCGLogger.debugExec {abort()}
              XCGLogger.error("Reached default case while applying object changes. This should not happen: Type \(type.rawValue)")
              break
            }
          }
          }, completion: nil)
      } else {
        logger.info("No need to apply batch changes because model did not change")
      }
      
      // Reset the dictionaries
      sectionChanges[.Insert] = NSMutableIndexSet() as NSMutableIndexSet
      sectionChanges[.Delete] = NSMutableIndexSet() as NSMutableIndexSet
      
      objectChanges[.Insert] = [NSIndexPath]()
      objectChanges[.Delete] = [NSIndexPath]()
      objectChanges[.Update] = [NSIndexPath]()
    }
  }
}
