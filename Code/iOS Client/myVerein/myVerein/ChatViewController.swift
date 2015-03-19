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

class ChatViewController: JSQMessagesViewController {
  
  struct ChatViewConstants {
    static let BatchSize = 20
    static let Entity = MessageRepository.MessageConstants.ClassName
    static let PredicateField = MessageRepository.MessageConstants.DivisionField
    static let SortField = MessageRepository.MessageConstants.TimestampField
    static let CacheName = "myVerein.ChatViewCache"
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
      cacheName: ChatViewConstants.CacheName)
    
    //controller.delegate = self
    return controller
  }()
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    senderDisplayName = "Hello"
    senderId = "1"
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

// MARK: - JSQMessagesCollectionViewDataSource
extension ChatViewController: JSQMessagesCollectionViewDataSource {
  override func collectionView(collectionView: JSQMessagesCollectionView!, messageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageData! {
    <#code#>
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, messageBubbleImageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageBubbleImageDataSource! {
    <#code#>
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, avatarImageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageAvatarImageDataSource! {
    <#code#>
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForCellTopLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    <#code#>
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForMessageBubbleTopLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    <#code#>
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForCellBottomLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    <#code#>
  }
}

// MARK: - JSQMessagesCollectionViewDelegateFlowLayout
extension ChatViewController: JSQMessagesCollectionViewDelegateFlowLayout {
  override func collectionView(collectionView: JSQMessagesCollectionView!, didTapAvatarImageView avatarImageView: UIImageView!, atIndexPath indexPath: NSIndexPath!) {
    <#code#>
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, didTapCellAtIndexPath indexPath: NSIndexPath!, touchLocation: CGPoint) {
    <#code#>
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, didTapMessageBubbleAtIndexPath indexPath: NSIndexPath!) {
    <#code#>
  }
}
