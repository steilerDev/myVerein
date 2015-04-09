//
//  ChatViewController.swift
//  myVerein
//
//  Created by Frank Steiler on 13/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import JSQMessagesViewController
import XCGLogger
import CoreData
import SwiftyUserDefaults

class ChatViewController: JSQMessagesViewController {
  
  struct ChatViewConstants {
    static let BatchSize = 20
    static let Entity = MessageConstants.ClassName
    static let PredicateField = MessageConstants.Fields.Division
    static let SortField = MessageConstants.Fields.Timestamp
    static let CacheName = "myVerein.ChatViewCache."
  }
  
  let logger = XCGLogger.defaultInstance()
  
  var division: Division!
  
  // Lazily initiating fetched result controller
  lazy var fetchedResultController: NSFetchedResultsController = {
    //Initializing data source (NSFetchedResultController)
    let fetchRequest = NSFetchRequest(entityName: ChatViewConstants.Entity)
    fetchRequest.fetchBatchSize = ChatViewConstants.BatchSize
    
    let sortDescriptor = NSSortDescriptor(key: ChatViewConstants.SortField, ascending: true)
    fetchRequest.sortDescriptors = [sortDescriptor]
    
    let predicate = NSPredicate(format: "\(ChatViewConstants.PredicateField) == %@", self.division)
    
    fetchRequest.predicate = predicate
    var controller = NSFetchedResultsController(fetchRequest: fetchRequest,
      managedObjectContext: (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!,
      sectionNameKeyPath: nil,
      cacheName: ChatViewConstants.CacheName + NSUUID().UUIDString
    )
    
    controller.delegate = self
    return controller
  }()
  
  // MARK: NSFetchedResultsControllerDelegate (Needed variables)
  
  /// Dictionary used to collect object changes
  private lazy var objectChanges: [NSFetchedResultsChangeType: [NSIndexPath]] = {
    // Setting up objects in dict
    var dict = [NSFetchedResultsChangeType: [NSIndexPath]]()
    dict[.Insert] = [NSIndexPath]()
    dict[.Delete] = [NSIndexPath]()
    return dict
    }()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    title = division.name
    inputToolbar.contentView.rightBarButtonItem.setToDefaultColor()
    inputToolbar.contentView.leftBarButtonItem = nil
    
    logger.debug("Loading user")
    let userRepository = UserRepository()
    if let senderId = Defaults[MVUserDefaultsConstants.UserID].string,
      sender = userRepository.getOrCreateUserFrom(id: senderId).user
    {
      logger.debug("Successfully loaded user")
      self.senderId = senderId
      senderDisplayName = sender.displayName
    } else {
      logger.error("Unable to load user")
      logger.debugExec { abort() }
      
      senderDisplayName = "42"
      senderId = "42"
    }
    
    // Accessing fetched result controller and therfore initiating it if it did not happen yet
    var error: NSError? = nil
    if fetchedResultController.performFetch(&error) {
      logger.info("Successfully initiated chat view data source for division \(division.id)")
    } else {
      logger.error("Unable to initiate chat view data source for division \(division.id): \(error?.localizedDescription)")
    }
    
    collectionView.collectionViewLayout.outgoingAvatarViewSize = CGSizeZero
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

// MARK: - UICollectionViewDataSource
extension ChatViewController {
  override func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
    return (fetchedResultController.sections?[section] as? NSFetchedResultsSectionInfo)?.numberOfObjects ?? 0
  }
  
  override func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
    let cell = super.collectionView(collectionView, cellForItemAtIndexPath: indexPath) as! JSQMessagesCollectionViewCell
    if let message = fetchedResultController.objectAtIndexPath(indexPath) as? Message {
      if message.isOutgoingMessage {
        cell.textView.textColor = UIColor.blackColor()
      } else {
        cell.textView.textColor = UIColor.whiteColor()
      }
    }
    
    return cell
  }
}

// MARK: - JSQMessagesCollectionViewDataSource
extension ChatViewController {
  override func collectionView(collectionView: JSQMessagesCollectionView!, messageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageData! {
    return fetchedResultController.objectAtIndexPath(indexPath) as! Message
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, messageBubbleImageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageBubbleImageDataSource! {
    let factory = JSQMessagesBubbleImageFactory()
    if let message = fetchedResultController.objectAtIndexPath(indexPath) as? Message {
      if message.isOutgoingMessage {
        return factory.outgoingMessagesBubbleImageWithColor(UIColor(hex: MVColor.Gray.Lighter))
      } else {
        return factory.incomingMessagesBubbleImageWithColor(UIColor(hex: MVColor.Primary.Normal))
      }
    } else {
      logger.severe("Unable to get message")
      logger.debugExec { abort() }
      return nil
    }
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, avatarImageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageAvatarImageDataSource! {
    if let message = fetchedResultController.objectAtIndexPath(indexPath) as? Message {
      return JSQMessagesAvatarImageFactory.avatarImageForUser(message.sender)
    } else {
      return nil
    }
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForCellTopLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    if let message = fetchedResultController.objectAtIndexPath(indexPath) as? Message {
      if !message.isOutgoingMessage {
        return NSAttributedString(string: message.sender.displayName)
      }
    }
    return nil
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForMessageBubbleTopLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    return nil
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForCellBottomLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    return nil
  }
}

// MARK: - JSQMessagesCollectionViewDelegateFlowLayout
extension ChatViewController {
  override func collectionView(collectionView: JSQMessagesCollectionView!, didTapAvatarImageView avatarImageView: UIImageView!, atIndexPath indexPath: NSIndexPath!) {
    println("Hello")
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, didTapCellAtIndexPath indexPath: NSIndexPath!, touchLocation: CGPoint) {
    println("Hello")
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, didTapMessageBubbleAtIndexPath indexPath: NSIndexPath!) {
    println("Hello")
  }
}

// MARK: - JSQMessagesViewController click events
extension ChatViewController {
  override func didPressAccessoryButton(sender: UIButton!) {
    println("Hello")
  }
  
  override func didPressSendButton(button: UIButton!, withMessageText text: String!, senderId: String!, senderDisplayName: String!, date: NSDate!) {
    logger.debug("Sending message with text \(text) and timestamp \(date) to division \(division)")
    let messageRepository = MessageRepository()
    let message = messageRepository.createMessage(text, id: NSUUID().UUIDString, timestamp: date, division: division, sender: UserRepository.getCurrentUser()!)
    messageRepository.save()
    finishSendingMessage()
    
    MVNetworkingHelper.sendMessage(message)
  }
}

// MARK: - NSFetchedResultsControllerDelegate
extension ChatViewController: NSFetchedResultsControllerDelegate {
  /// Collecting object changes in dictionary and applying them in a batch after they have all been applied
  func controller(controller: NSFetchedResultsController, didChangeObject anObject: AnyObject, atIndexPath indexPath: NSIndexPath?, forChangeType type: NSFetchedResultsChangeType, newIndexPath: NSIndexPath?) {
    logger.debug("Object in chat view changed.")
    if let collectionView = self.collectionView {
      switch type {
      case .Insert:
        if let currentIndexPath = newIndexPath {
          objectChanges[type]?.append(currentIndexPath)
        } else {
          logger.error("Unable to insert object in chat view")
        }
      case .Delete:
        if let currentIndexPath = indexPath {
          objectChanges[type]?.append(currentIndexPath)
        } else {
          logger.error("Unable to delete object in chat view")
        }
      case .Update:
        XCGLogger.error("Reached update case while applying object changes in chat view. Ignoring changes")
      case .Move:
        if let currentIndexPath = newIndexPath, oldIndexPath = indexPath {
          objectChanges[.Delete]?.append(oldIndexPath)
          objectChanges[.Insert]?.append(currentIndexPath)
        } else {
          logger.error("Unable to move object in chat view")
        }
      }
    } else {
      logger.error("Unable to change object in chat view")
    }
  }
  
  /// Batch changing controller after the content finished changing. This needs to be done since CollectionView and NSFetchedResultsController don't play nicely. See https://github.com/ashfurrow/UICollectionView-NSFetchedResultsController
  func controllerDidChangeContent(controller: NSFetchedResultsController) {
    logger.debug("Applying batch changes because model changed")
    if let currentCollectionView = collectionView {
      // Apply section changes
      if !(objectChanges[.Insert]?.isEmpty ?? true) || !(objectChanges[.Delete]?.isEmpty ?? true) || !(objectChanges[.Update]?.isEmpty ?? true) { //Applying object changes only if there was no section changes
        logger.debug("Applying object changes")
        currentCollectionView.performBatchUpdates({
          for (type, indexPath) in self.objectChanges {
            switch type {
            case .Insert:
              currentCollectionView.insertItemsAtIndexPaths(indexPath)
            case .Delete:
              currentCollectionView.deleteItemsAtIndexPaths(indexPath)
            case .Update:
              XCGLogger.debugExec {abort()}
              XCGLogger.error("Reached update case while applying object changes in chat view. This should not happen")
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
      objectChanges[.Insert] = [NSIndexPath]()
      objectChanges[.Delete] = [NSIndexPath]()
    }
  }
}


extension JSQMessagesAvatarImageFactory {
  class func avatarImageForUser(user: User) -> JSQMessagesAvatarImage! {
    let constDiameter = UInt(34)
    if let userImage = user.avatar {
      return avatarImageWithImage(userImage, diameter: constDiameter)
    } else {
      return avatarImageWithUserInitials(user.initials, backgroundColor: UIColor(hex: MVColor.Primary.Light), textColor: UIColor.whiteColor(), font: UIFont.systemFontOfSize(14), diameter: constDiameter)
    }
  }
}