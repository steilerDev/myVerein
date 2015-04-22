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
//  AppDelegate.swift
//  This class is an entry point for the application and is available within the whole context. It enables system wide function and sets up the core data stack.
//

import UIKit
import CoreData
import XCGLogger
import SwiftyUserDefaults

@UIApplicationMain
class AppDelegate: UIResponder {
  
  var window: UIWindow?
  let logger = XCGLogger.defaultInstance()
  
  // This variable references the threading object as long as the application should repeatedly check its messages and calendar changes
  var timerObject: MarshalThreadingObject?
  
  // MARK: - Core Data stack
  
  lazy var applicationDocumentsDirectory: NSURL = {
    // The directory the application uses to store the Core Data store file. This code uses a directory named "de.steilerdev.myverein.ios.myVerein" in the application's documents Application Support directory.
    let urls = NSFileManager.defaultManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask)
    return urls[urls.count-1] as! NSURL
    }()
  
  lazy var managedObjectModel: NSManagedObjectModel = {
    XCGLogger.verbose("Loading managed object model")
    // The managed object model for the application. This property is not optional. It is a fatal error for the application not to be able to find and load its model.
    let modelURL = NSBundle.mainBundle().URLForResource("myVereinModel", withExtension: "momd")!
    return NSManagedObjectModel(contentsOfURL: modelURL)!
    }()
  
  lazy var persistentStoreCoordinator: NSPersistentStoreCoordinator? = createPersistentStoreCoordinator(self)()
  
  lazy var managedObjectContext: NSManagedObjectContext? = createManagedObjectContext(self)()
}

// MARK: - UIAppliationDelegate function (application lifecycle methods)
extension AppDelegate: UIApplicationDelegate {
  func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
    // Setting up the logger
    logger.setup(logLevel: .Debug, showLogLevel: true, showFileNames: true, showLineNumbers: true, writeToFile: nil)
    
    // Registering notification
    logger.debug("Trying to register for notification/checking if notifications are still available")
    let notificationSettings = UIUserNotificationSettings(forTypes: .Alert | .Sound | .Alert, categories: nil)
    UIApplication.sharedApplication().registerUserNotificationSettings(notificationSettings)    
    return true
  }
  
  func application(application: UIApplication, didRegisterUserNotificationSettings notificationSettings: UIUserNotificationSettings) {
    logger.info("Application did register user notification settings: \(notificationSettings)")
    if notificationSettings.types == .None {
      logger.error("No notifications allowed!")
    } else {
      logger.info("Application allows notification, updating device token")
      application.registerForRemoteNotifications()
    }
  }
  
  func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
    logger.info("Application did register for remote notifications, updating device token (\(deviceToken))")
    MVNetworkingHelper.updateDeviceToken(deviceToken)
  }
  
  func application(application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: NSError) {
    logger.error("Application failed to register for remote notification: \(error.extendedDescription)")
  }
  
  func application(application: UIApplication, didReceiveLocalNotification notification: UILocalNotification) {
    logger.info("Received local notification, showing as in app notification")
    MVDropdownAlertCenter.instance.showNotification(MVDropdownAlertObject(localNotification: notification))
  }
  
  func applicationDidBecomeActive(application: UIApplication) {
    UIApplication.sharedApplication().applicationIconBadgeNumber = 0
    logger.info("Starting background thread, which is syncing the application with the server")
    timerObject = {
      self.logger.debug("Syncing system")
      MVNetworkingHelper.syncMessages()
      MVNetworkingHelper.syncUserDivision()
      MVNetworkingHelper.syncUserEvent(nil)
      self.logger.debug("Finished sync")
    }<~
  }
  
  func applicationWillResignActive(application: UIApplication) {
    logger.info("Stopping background thread, which is syncing the application with the server")
    timerObject = nil
  }
  
  func applicationWillTerminate(application: UIApplication) {
    self.saveContext()
  }
}

// MARK: - CoreData functions
extension AppDelegate {
  
  private func createPersistentStoreCoordinator() -> NSPersistentStoreCoordinator? {
    // The persistent store coordinator for the application. This implementation creates and return a coordinator, having added the store for the application to it. This property is optional since there are legitimate error conditions that could cause the creation of the store to fail.
    // Create the coordinator and store
    var coordinator: NSPersistentStoreCoordinator? = NSPersistentStoreCoordinator(managedObjectModel: managedObjectModel)
    let url = applicationDocumentsDirectory.URLByAppendingPathComponent("myVereinModel.sqlite")
    var error: NSError?
    let mOptions = [NSMigratePersistentStoresAutomaticallyOption: true,
      NSInferMappingModelAutomaticallyOption: true]
    if coordinator!.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: url, options: mOptions, error: &error) == nil {
      // Report any error we got.
      error = MVError.createError(.MVLocalDatabaseLoadingError, failureReason: nil, underlyingError: error)
      logger.severe("Unresolved error during application initialization: \(error!.extendedDescription)")
      logger.debugExec { abort() } // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
      return nil
    } else {
      return coordinator
    }
  }
  
  private func createManagedObjectContext() -> NSManagedObjectContext? {
    if let coordinator = persistentStoreCoordinator {
      var managedObjectContext = NSManagedObjectContext()
      managedObjectContext.persistentStoreCoordinator = coordinator
      return managedObjectContext
    } else {
      return nil
    }
  }
  
  func flushDatabase() {
    logger.debug("Flushing database")
    if let managedObjectContext = managedObjectContext {
      if let persistentStoreCoordinator = self.persistentStoreCoordinator, stores = persistentStoreCoordinator.persistentStores as? [NSPersistentStore] {
        for store in stores {
          var error: NSError?
          if !persistentStoreCoordinator.removePersistentStore(store, error: &error) {
            logger.error("Unable to remove persistent store \(error?.extendedDescription)")
          } else {
            logger.debug("Successfully removed persistent store \(store)")
          }
          if let storeURL = store.URL?.path {
            if !NSFileManager.defaultManager().removeItemAtPath(storeURL, error: &error) {
              logger.error("Unable to remove item at path \(storeURL): \(error?.extendedDescription)")
            } else {
              logger.debug("Successfully removed item at path \(storeURL)")
            }
          } else {
            logger.error("Unable to delete store, because the store URL could not be retrieved")
          }
        }
      } else {
        logger.error("Unable to get persistent stores")
      }
    } else {
      logger.error("Unable to get managed object context")
    }
    persistentStoreCoordinator = createPersistentStoreCoordinator()
    managedObjectContext = createManagedObjectContext()
    logger.info("Successfully flushed database")
    
    logger.debug("Resetting user defaults storing last sync timestamps")
    Defaults[MVUserDefaultsConstants.LastSynced.Event] = nil
  }
  
  func saveContext () {
    if let moc = self.managedObjectContext {
      var error: NSError?
      if moc.hasChanges && !moc.save(&error) {
        logger.severe("Unresolved error \(error?.extendedDescription)")
        logger.debugExec { abort() } // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
      }
    }
  }
}

// MARK: - Shared application specific functionalities
extension AppDelegate {
  
  /// This function is presenting the login view upon the currently shown view controller
  func showLoginView() {
    if let currentViewController = window?.rootViewController as? UITabBarController,
      loginViewController = currentViewController.storyboard?.instantiateViewControllerWithIdentifier(LoginViewController.StoryBoardID) as? LoginViewController
    {
        logger.info("Showing log in screen")
        currentViewController.presentViewController(loginViewController, animated: true, completion: nil)
    } else {
      logger.severe("Unable to show log in screen")
    }
  }
  
  /// This function is logging out the current user (removing his credentials from the keychain) and presenting the login view.
  func logoutUser() {
    logger.info("Performing logout")
    MVSecurity.instance.updateKeychain(nil, newPassword: nil, newDomain: nil)
    showLoginView()
  }
}
