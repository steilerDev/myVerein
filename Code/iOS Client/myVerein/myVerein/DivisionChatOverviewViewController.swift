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

class DivisionChatOverviewViewController: UICollectionViewController, NSFetchedResultsControllerDelegate {
    
    var user = ["Rugby 1st team", "Soccer", "Soccer 2nd team", "Soccer 1st team"]
    struct DivisionChatOverviewConstants {
        static let BatchSize = 20
        static let Entity = DivisionRepository.DivisionConstants.ClassName
        static let PredicateField = DivisionRepository.DivisionConstants.UserMembershipStatus
        static let SortField = DivisionRepository.DivisionConstants.Name
        static let CacheName = "myVerein.ChatOverviewCache"
        static let ReuseCellIdentifier = "ChatCell"
    }
    
    let logger = XCGLogger.defaultInstance()
    
    // Lazily initiating fetched result controller
    lazy var fetchedResultController: NSFetchedResultsController = {
        //Initializing data source (NSFetchedResultController)
        let fetchRequest = NSFetchRequest(entityName: DivisionChatOverviewConstants.Entity)
        fetchRequest.fetchBatchSize = DivisionChatOverviewConstants.BatchSize
        
        /// TODO: Improve sort
        let sortDescriptor = NSSortDescriptor(key: DivisionChatOverviewConstants.SortField, ascending: true)
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
            cacheName: DivisionChatOverviewConstants.CacheName)
        
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

    // MARK: - Navigation
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using [segue destinationViewController].
        // Pass the selected object to the new view controller.
    }

    // MARK: UICollectionViewDataSource

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
    
    private func configureCell(indexPath: NSIndexPath, cell: DivisionChatCell) {
        if let divisionObject = fetchedResultController.objectAtIndexPath(indexPath) as? Division {
            cell.divisionLabel.text = divisionObject.name
            cell.lastUser = divisionObject.name
        } else {
            logger.error("Unable to configure cell using default values")
            cell.divisionLabel.text = "Division"
            cell.lastUser = "Division"
        }
    }
    
    // MARK: UICollectionViewDelegate

    override func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        var selectedCell = collectionView.cellForItemAtIndexPath(indexPath) as? DivisionChatCell
        selectedCell?.notificationCount++
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
    
    // MARK: NSFetchedResultsControllerDelegate
    
    /*
    Assume self has a property 'tableView' -- as is the case for an instance of a UITableViewController
    subclass -- and a method configureCell:atIndexPath: which updates the contents of a given cell
    with information from a managed object at the given index path in the fetched results controller.
    */
    
    func controller(controller: NSFetchedResultsController, didChangeSection sectionInfo: NSFetchedResultsSectionInfo, atIndex sectionIndex: Int, forChangeType type: NSFetchedResultsChangeType) {
        switch type {
            case .Insert:
                self.collectionView?.insertSections(NSIndexSet(index: sectionIndex))
            case .Delete:
                self.collectionView?.deleteSections(NSIndexSet(index: sectionIndex))
            default:
                break;
        }
    }
    
    func controller(controller: NSFetchedResultsController, didChangeObject anObject: AnyObject, atIndexPath indexPath: NSIndexPath?, forChangeType type: NSFetchedResultsChangeType, newIndexPath: NSIndexPath?) {
        if let collectionView = self.collectionView {
            switch type {
            case .Insert:
                if let currentIndexPath = newIndexPath {
                    collectionView.insertItemsAtIndexPaths([currentIndexPath])
                }
            case .Delete:
                if let currentIndexPath = indexPath {
                    collectionView.deleteItemsAtIndexPaths([currentIndexPath])
                }
            case .Update:
                if let currentIndexPath = indexPath, currentCell = collectionView.cellForItemAtIndexPath(currentIndexPath) as? DivisionChatCell {
                    configureCell(currentIndexPath, cell: currentCell)
                }
            case .Move:
                if let currentIndexPath = newIndexPath, oldIndexPath = indexPath {
                    collectionView.deleteItemsAtIndexPaths([oldIndexPath])
                    collectionView.insertItemsAtIndexPaths([currentIndexPath])
                }
            }
        }
    }
}
