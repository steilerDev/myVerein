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
//  ChatViewController.swift
//  This file holds all information related to the chat view, including appearance modifications, data source for the JSQMessageViewController and delegate methods for click events.
//  The chat view is the page presenting the messages of a division chat in descending order starting with the oldest message. The user's avatar either shows the user's avatar or his initials.
//

import UIKit
import JSQMessagesViewController
import XCGLogger
import CoreData
import SwiftyUserDefaults

//MARK: - Plain ChatsViewController holding all variables and outlets for the class
class ChatViewController: JSQMessagesViewController {
  
  let logger = XCGLogger.defaultInstance()
  
  var division: Division!
  
  // The presenting view controller (DivisionChatOverviewViewController) and cell of the overview as the notification delegate to notify about read messages
  var notificationDelegates = [NotificationCountDelegate?]()
  
  /// The token handed over by the notification subscription, stored to be able to release resources.
  var divisionNotificationObserverToken: NSObjectProtocol?
  var messageNotificationObserverToken: NSObjectProtocol?
  
  /// The message bubble factories, for tailles message bubbles and ones with tails
  let taillesMessageBubbleFactory = JSQMessagesBubbleImageFactory(bubbleImage: UIImage.jsq_bubbleRegularTaillessImage(), capInsets: UIEdgeInsetsZero)
  let regularMessageBubbleFactory = JSQMessagesBubbleImageFactory(bubbleImage: UIImage.jsq_bubbleRegularImage(), capInsets: UIEdgeInsetsZero)
  let outgoingMessageBubbleColor = UIColor(hex: MVColor.Gray.Lighter)
  let incomingMessageBubbleColor = UIColor(hex: MVColor.Primary.Normal)
  
  // Lazily initiating fetched result controller
  lazy var fetchedResultController: NSFetchedResultsController = {
    //Initializing data source (NSFetchedResultController)
    let fetchRequest = NSFetchRequest(entityName: ChatViewConstants.Entity)
    fetchRequest.fetchBatchSize = ChatViewConstants.BatchSize
    
    let sortDescriptor = NSSortDescriptor(key: ChatViewConstants.SortField, ascending: true)
    fetchRequest.sortDescriptors = [sortDescriptor]
    
    let predicate = NSPredicate(format: "\(ChatViewConstants.PredicateField) == %@", self.division)
    
    fetchRequest.predicate = predicate
    // The cache name should be unique so it does not conflict with other chats
    var controller = NSFetchedResultsController(fetchRequest: fetchRequest,
      managedObjectContext: (UIApplication.sharedApplication().delegate as! AppDelegate).managedObjectContext!,
      sectionNameKeyPath: nil,
      cacheName: ChatViewConstants.CacheName + NSUUID().UUIDString
    )
    
    controller.delegate = self
    return controller
  }()
  
  // MARK: Variables needed by the NSFetchedResultsControllerDelegate
  
  /// Dictionary used to collect object changes
  private lazy var objectChanges: [NSFetchedResultsChangeType: [NSIndexPath]] = {
    // Setting up objects in dict
    var dict = [NSFetchedResultsChangeType: [NSIndexPath]]()
    dict[.Insert] = [NSIndexPath]()
    dict[.Delete] = [NSIndexPath]()
    return dict
  }()
}

// MARK: - UIViewController lifecycle methods
extension ChatViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    if division == nil {
      logger.error("Unable to load division, dismissing view")
      dismissViewControllerAnimated(true, completion: nil)
    } else {
      // Adjusting appearance
      title = division.name
      inputToolbar.contentView.rightBarButtonItem.setToDefaultColor()
      inputToolbar.contentView.leftBarButtonItem = nil
      collectionView.collectionViewLayout.outgoingAvatarViewSize = CGSizeZero
      
      logger.debug("Loading user")
      if let sender = UserRepository.getCurrentUser()
      {
        logger.debug("Successfully loaded user")
        self.senderId = sender.id
        senderDisplayName = sender.displayName
      } else {
        logger.error("Unable to load user")
        logger.debugExec { abort() }
        dismissViewControllerAnimated(true, completion: nil)
        return
      }
      
      // Checking if user is still allowed to post in this chat
      if !(division.userMembershipStatus == .Member) {
        logger.info("User is not member of this chat, disabling it")
        disableChat()
      }
      
      // Accessing fetched result controller and therfore initiating it if it did not happen yet
      var error: NSError?
      if fetchedResultController.performFetch(&error) {
        logger.info("Successfully initiated chat view data source for division \(self.self.division.id)")
      } else {
        logger.error("Unable to initiate chat view data source for division \(self.division.id): \(error?.extendedDescription)")
      }
    }
  }
  
  /// Within this function the notification observer subscribes to the notification system. Doing this in view will appear because view did appear is somehow not called.
  override func viewWillAppear(animated: Bool) {
    super.viewDidAppear(animated)
    super.viewWillAppear(animated)
    // This observer is monitoring the division this chat is associated with. If the user's membership is changing this observer will be notified and disable the chat.
    logger.debug("Division chat for \(self.division) is subscribing to notification system")
    divisionNotificationObserverToken = MVNotification.subscribeToDivisionSyncCompletedNotificationForDivision(division) {
      notification in
      if let changedDivision = notification.object as? Division {
        let logger = XCGLogger.defaultInstance()
        logger.debug("Received notification for \(changedDivision)")
        if !(changedDivision.userMembershipStatus == .Member) {
          logger.info("User is no longer member of this chat, disabling it")
          self.disableChat()
        }
      }
    }
    messageNotificationObserverToken = MVNotification.subscribeToMessageSyncCompletedNotificationForDivisionChat(division){
      notification in
      XCGLogger.debug("Received notification token, reloading messages")
      JSQSystemSoundPlayer.jsq_playMessageReceivedSound()
      self.finishReceivingMessageAnimated(true)
    }
  }
  
  /// Within this funciton the notification observer un-subscribes from the notification system.
  override func viewWillDisappear(animated: Bool) {
    super.viewWillDisappear(animated)
    
    logger.debug("Division chat for \(self.division) is un-subscribing from notification system")
    if let divisionNotificationObserverToken = divisionNotificationObserverToken {
      MVNotification.unSubscribeFromNotification(divisionNotificationObserverToken)
    }
    if let messageNotificationObserverToken = messageNotificationObserverToken {
      MVNotification.unSubscribeFromNotification(messageNotificationObserverToken)
    }
  }

  
  /*
  // MARK: Navigation
  
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
    
    // Hide avatar image if there are several messages of the same user in a row
    cell.avatarImageView.hidden = compareNextCellAndCellAtIndexPath(indexPath, andReturnExpresionResult: { $0.senderId() == $1.senderId() && $0.senderId() != self.senderId })
    
    // Hide tail of cell if there are sevral messages of the same user in a row
    if let newMessageBubbleImage = compareNextCellAndCellAtIndexPath(indexPath,
        andReturnValue: {
          return $0.isOutgoingMessage ?
            self.taillesMessageBubbleFactory.outgoingMessagesBubbleImageWithColor(self.outgoingMessageBubbleColor):
            self.taillesMessageBubbleFactory.incomingMessagesBubbleImageWithColor(self.incomingMessageBubbleColor)
        },
        ifExpresionHoldsTrue: { $0.senderId() == $1.senderId() }
      )
    {
      cell.messageBubbleImageView.image = newMessageBubbleImage.messageBubbleImage
      cell.messageBubbleImageView.highlightedImage = newMessageBubbleImage.messageBubbleHighlightedImage
    }
    
    return cell
  }
}

// MARK: - JSQMessagesCollectionViewDataSource
extension ChatViewController {
  override func collectionView(collectionView: JSQMessagesCollectionView!, messageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageData! {
    if let message = fetchedResultController.objectAtIndexPath(indexPath) as? Message {
      if !message.read {
        //Updating read flag and notification count
        logger.debug("Setting read flag for message \(message)")
        message.read = true
        MessageRepository().save()
        for delegate in notificationDelegates {
          if delegate == nil {
            logger.debug("Not able to delegate new count because the delegate is nil")
          } else if let cell = delegate as? DivisionChatCell where cell.division != self.division {
            logger.warning("Not able delegate new count to cell because divisions are different")
          } else {
            delegate!.decrementNotificationCountBy(1, sender: self)
          }
        }
      }
      return message
    } else {
      logger.severe("Unable to get message")
      logger.debugExec { abort() }
      dismissViewControllerAnimated(true, completion: nil)
      return nil
    }
  }
  
  // MARK: Image data
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, messageBubbleImageDataForItemAtIndexPath indexPath: NSIndexPath!) -> JSQMessageBubbleImageDataSource! {
    let factory = JSQMessagesBubbleImageFactory()
    if let message = fetchedResultController.objectAtIndexPath(indexPath) as? Message {
      if message.isOutgoingMessage {
        return factory.outgoingMessagesBubbleImageWithColor(outgoingMessageBubbleColor)
      } else {
        return factory.incomingMessagesBubbleImageWithColor(incomingMessageBubbleColor)
      }
    } else {
      logger.severe("Unable to get message")
      logger.debugExec { abort() }
      dismissViewControllerAnimated(true, completion: nil)
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
  
  // MARK: Message bubble top label (sender)
  
  /// This function returns the user shown on top of a message bubble. If this function returns nil the label should not be shown. The user's label is only shown if the previous message's sender is not this message's sender. The label of messages send from the application's user are never shown.
  ///
  /// :param: indexPath The index path of the cell.
  /// :param: collectionView The collection view the cell is in.
  /// :returns: The user that should be shown in the label, or nil if nothing should be shown.
  func userLabelForMessageAtIndexPath(indexPath: NSIndexPath, inCollectionView collectionView: JSQMessagesCollectionView) -> User? {
    return comparePreviousCellAndCellAtIndexPath(indexPath,
      andReturnValue: { $0.sender },
      ifExpresionHoldsTrue: { $1.senderId() != self.senderId && $0.senderId() != $1.senderId() }
    )
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForMessageBubbleTopLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    if let user = userLabelForMessageAtIndexPath(indexPath, inCollectionView: collectionView) {
      return NSAttributedString(string: user.displayName)
    } else {
      return super.collectionView(collectionView, attributedTextForCellTopLabelAtIndexPath: indexPath)
    }
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, layout collectionViewLayout: JSQMessagesCollectionViewFlowLayout!, heightForMessageBubbleTopLabelAtIndexPath indexPath: NSIndexPath!) -> CGFloat {
    if userLabelForMessageAtIndexPath(indexPath, inCollectionView: collectionView) != nil {
      return kJSQMessagesCollectionViewCellLabelHeightDefault
    } else {
      return super.collectionView(collectionView, layout: collectionViewLayout, heightForCellTopLabelAtIndexPath: indexPath)
    }
  }
  
  // MARK: Cell top label (timestamp)
  
  /// This function returns the date shown on top of a cell. If this function returns nil the label should not be shown. The timestamp is only shown if this message and the previous one are more than the value defined in the 'TimeBetweenMessagesThresholdForLable' - constant apart, or if it is the top cell.
  ///
  /// :param: indexPath The index path of the cell.
  /// :param: collectionView The collection view the cell is in.
  /// :returns: The date that should be shown in the label, or nil if nothing should be shown.
  func timestampLabelForMessageAtIndexPath(indexPath: NSIndexPath, inCollectionView collectionView: JSQMessagesCollectionView) -> NSDate? {
    return comparePreviousCellAndCellAtIndexPath(indexPath,
      andReturnValue: { $0.timestamp },
      ifExpresionHoldsTrue: {$0.timestamp.dateByAddingTimeInterval(ChatViewConstants.TimeBetweenMessagesThresholdForLabel).isBefore($1.timestamp)}
    )
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, attributedTextForCellTopLabelAtIndexPath indexPath: NSIndexPath!) -> NSAttributedString! {
    if let timestamp = timestampLabelForMessageAtIndexPath(indexPath, inCollectionView: collectionView) {
      return JSQMessagesTimestampFormatter.sharedFormatter().attributedTimestampForDate(timestamp)
    } else {
      return super.collectionView(collectionView, attributedTextForCellTopLabelAtIndexPath: indexPath)
    }
  }
  
  override func collectionView(collectionView: JSQMessagesCollectionView!, layout collectionViewLayout: JSQMessagesCollectionViewFlowLayout!, heightForCellTopLabelAtIndexPath indexPath: NSIndexPath!) -> CGFloat {
    if timestampLabelForMessageAtIndexPath(indexPath, inCollectionView: collectionView) != nil {
      return kJSQMessagesCollectionViewCellLabelHeightDefault
    } else {
      return super.collectionView(collectionView, layout: collectionViewLayout, heightForCellTopLabelAtIndexPath: indexPath)
    }
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
    logger.debug("Sending message with text \(text) and timestamp \(date) to division \(self.division)")
    let messageRepository = MessageRepository()
    let message = messageRepository.createMessage(text, timestamp: date, division: division, sender: UserRepository.getCurrentUser()!)
    messageRepository.save()
    logger.debug("Saved prototype message and saved database, showing message in view")
    JSQSystemSoundPlayer.jsq_playMessageSentSound()
    self.finishSendingMessageAnimated(true)
    
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

// MARK: - ChatView lifecycle methods
extension ChatViewController {
  /// This function is called if the user is no longer part of the chat and disables any input.
  func disableChat() {
    inputToolbar.toggleSendButtonEnabled()
    inputToolbar.contentView.textView.placeHolder = "You are no longer part of this division"
    inputToolbar.contentView.textView.editable = false
  }
  
  override func finishReceivingMessageAnimated(animated: Bool) {
    super.finishReceivingMessageAnimated(animated)
    // TODO: Implement checking if avatar needs to disappear & bubble image update, maybe animated? (Fade out or move down?
  }
}

// MARK: - Utility functions
extension ChatViewController {
  /// This function compares the current cell identified by the index path with its predecessor using the provided closure. If the closure returns true or the message does not have a predecessor, the function will return the evaluation of the provided (auto-)closure.
  ///
  /// :param: indexPath The index path defining the current cell.
  /// :param: closure The evaluation of this closure is used to decide wheter or not to return the value. The first argument is the message within the preceding cell, where the second one is the message within the current cell.
  /// :param: returnValue The return value returned if the expresion holds true or the cell does not have a predecessor. The message of the current cell is available as paramter.
  /// :returns: The result of the return-value-closure if the provided expresion holds true or the cell does not have a predecessor.
  func comparePreviousCellAndCellAtIndexPath<T>(indexPath: NSIndexPath, andReturnValue returnValue:  (Message) -> T, ifExpresionHoldsTrue closure: (Message, Message) -> Bool) -> T? {
    if let currentMessage = fetchedResultController.objectAtIndexPath(indexPath) as? Message {
      let prevIndexPath = indexPath.decrement()
      if collectionView.collectionView(collectionView, hasItemForIndexPath: prevIndexPath) {
        if let prevMessage = fetchedResultController.objectAtIndexPath(prevIndexPath) as? Message {
          if closure(prevMessage, currentMessage) {
            return returnValue(currentMessage)
          }
        }
      } else {
        return returnValue(currentMessage)
      }
    }
    return nil
  }
  
  /// This function compares the current cell identified by the index path with its predecessor using the provided closure. If the closure returns true or the message does not have a predecessor, the function will return true.
  ///
  /// :param: indexPath The index path defining the current cell.
  /// :param: closure The evaluation of this closure is used to decide wheter or not to return the value. The first message is one from the previous cell, where the second one is from the current cell.
  /// :returns: True if the provided expresion holds true or the cell does not have a predecessor, false otherwise.
  func comparePreviousCellAndCellAtIndexPath(indexPath: NSIndexPath, andReturnExpresionResult closure: (Message, Message) -> Bool) -> Bool {
    return comparePreviousCellAndCellAtIndexPath(indexPath,
      andReturnValue: { _ in return true },
      ifExpresionHoldsTrue: closure
      ) ?? false
  }
  
  /// This function compares the current cell identified by the index path with its successor using the provided closure. If the closure returns true the function will return the evaluation of the provided (auto-)closure.
  ///
  /// :param: indexPath The index path defining the current cell.
  /// :param: closure The evaluation of this closure is used to decide wheter or not to return the value. The first argument is the message within the current cell, the second argument is the message within the succeeding cell.
  /// :param: returnValue The return value returned if the expresion holds true or the cell does not have a predecessor. The message of the current cell is available as paramter.
  /// :returns: The result of the return-value-closure if the provided expresion holds true. If the cell does not have a successor the function will always return nil.
  func compareNextCellAndCellAtIndexPath<T>(indexPath: NSIndexPath, andReturnValue returnValue:  (Message) -> T, ifExpresionHoldsTrue closure: (Message, Message) -> Bool) -> T? {
    
    let prevIndexPath = indexPath.increment()
    if collectionView.collectionView(collectionView, hasItemForIndexPath: prevIndexPath) {
      if let nextMessage = fetchedResultController.objectAtIndexPath(prevIndexPath) as? Message,
        let currentMessage = fetchedResultController.objectAtIndexPath(indexPath) as? Message
      {
        if closure(currentMessage, nextMessage) {
          return returnValue(currentMessage)
        }
      }
    }
    return nil
  }
  
  /// This function compares the current cell identified by the index path with its successor using the provided closure. If the closure returns true the function will return true.
  ///
  /// :param: indexPath The index path defining the current cell.
  /// :param: closure The evaluation of this closure is used to decide wheter or not to return the value. The first argument is the message within the current cell, the second argument is the message within the succeeding cell.
  /// :returns: True if the provided expresion holds true, false otherwise. If the cell does not have a successor the function will always return false.
  func compareNextCellAndCellAtIndexPath(indexPath: NSIndexPath, andReturnExpresionResult closure: (Message, Message) -> Bool) -> Bool {
    return compareNextCellAndCellAtIndexPath(indexPath,
      andReturnValue: { _ in return true },
      ifExpresionHoldsTrue: closure
      ) ?? false
  }
}

// MARK: - Extension for the avatar image factory
/// This extension for the avatar image factory creates an avatar for a user either by using his stored picture or his initials.
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

// MARK: - ChatView related constants
struct ChatViewConstants {
  static let BatchSize = 20
  static let Entity = MessageConstants.ClassName
  static let PredicateField = MessageConstants.Fields.Division
  static let SortField = MessageConstants.Fields.Timestamp
  static let TimeBetweenMessagesThresholdForLabel = 1800.0 //in seconds
  static let CacheName = "myVerein.ChatViewCache."
}