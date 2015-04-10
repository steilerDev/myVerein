//
//  AppDelegate.swift
//  myVerein
//
//  Created by Frank Steiler on 25/02/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import CoreData
import XCGLogger
import SwiftyUserDefaults

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
  
  var window: UIWindow?
  
  let logger = XCGLogger.defaultInstance()
  
  func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
    // Override point for customization after application launch.
    
    UIApplication.sharedApplication().registerForRemoteNotifications()
        
    // Setting up the logger
    logger.setup(logLevel: .Debug, showLogLevel: true, showFileNames: true, showLineNumbers: true, writeToFile: nil)
    return true
  }
  
  func application(application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: NSData) {
    println("####################################################")
    println("Device token: \(deviceToken)")
  }
  
  func application(application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: NSError) {
    println("####################################################")
    println("Unable to register remote notification: \(error)")
  }
  
  func applicationWillResignActive(application: UIApplication) {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
  }
  
  func applicationDidEnterBackground(application: UIApplication) {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
  }
  
  func applicationWillEnterForeground(application: UIApplication) {
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
  }
  
  func applicationDidBecomeActive(application: UIApplication) {
    MVNetworkingHelper.syncMessages()
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
  }
  
  func applicationWillTerminate(application: UIApplication) {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    // Saves changes in the application's managed object context before the application terminates.
    self.saveContext()
  }
  
  
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
  
  private func createPersistentStoreCoordinator() -> NSPersistentStoreCoordinator? {
    // The persistent store coordinator for the application. This implementation creates and return a coordinator, having added the store for the application to it. This property is optional since there are legitimate error conditions that could cause the creation of the store to fail.
    // Create the coordinator and store
    var coordinator: NSPersistentStoreCoordinator? = NSPersistentStoreCoordinator(managedObjectModel: managedObjectModel)
    let url = applicationDocumentsDirectory.URLByAppendingPathComponent("myVereinModel.sqlite")
    var error: NSError? = nil
    let mOptions = [NSMigratePersistentStoresAutomaticallyOption: true,
      NSInferMappingModelAutomaticallyOption: true]
    if coordinator!.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: url, options: mOptions, error: &error) == nil {
      // Report any error we got.
      error = MVError.createError(.MVLocalDatabaseLoadingError, failureReason: nil, underlyingError: error)
      XCGLogger.severe("Unresolved error during application initialization: \(error), \(error!.userInfo)")
      XCGLogger.debugExec { abort() } // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
      return nil
    } else {
      return coordinator
    }
  }
  
  lazy var managedObjectContext: NSManagedObjectContext? = createManagedObjectContext(self)()
  
  private func createManagedObjectContext() -> NSManagedObjectContext? {
    if let coordinator = persistentStoreCoordinator {
      var managedObjectContext = NSManagedObjectContext()
      managedObjectContext.persistentStoreCoordinator = coordinator
      return managedObjectContext
    } else {
      return nil
    }
  }
  
  // MARK: - Core Data functions
  
  func flushDatabase() {
    logger.debug("Flushing database")
    if let managedObjectContext = managedObjectContext {
      if let persistentStoreCoordinator = self.persistentStoreCoordinator, stores = persistentStoreCoordinator.persistentStores as? [NSPersistentStore] {
        for store in stores {
          var error: NSError?
          if !persistentStoreCoordinator.removePersistentStore(store, error: &error) {
            logger.error("Unable to remove persistent store \(error?.localizedDescription)")
          } else {
            logger.debug("Successfully removed persistent store \(store)")
          }
          if let storeURL = store.URL?.path {
            if !NSFileManager.defaultManager().removeItemAtPath(storeURL, error: &error) {
              logger.error("Unable to remove item at path \(storeURL): \(error?.localizedDescription)")
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
      var error: NSError? = nil
      if moc.hasChanges && !moc.save(&error) {
        logger.severe("Unresolved error \(error), \(error!.userInfo)")
        logger.debugExec { abort() } // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
      }
    }
  }
  
  func showLoginView() {
    if let currentViewController = window?.rootViewController as? UITabBarController,
      loginViewController = currentViewController.storyboard?.instantiateViewControllerWithIdentifier(LoginViewController.StoryBoardID) as? LoginViewController
    {
        logger.info("Showing log in screen")
        currentViewController.presentViewController(loginViewController, animated: true, completion: {})
    } else {
      logger.severe("Unable to show log in screen")
    }
  }
  
}
