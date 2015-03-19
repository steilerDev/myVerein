//
//  NetworkingSessionFactory.swift
//  myVerein
//
//  Created by Frank Steiler on 03/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

import AFNetworking
import Locksmith
import XCGLogger

/// This class is used to manage the shared HTTPSession.
class MVNetworkingSessionFactory {
  
  private static let logger = XCGLogger.defaultInstance()
  
  private static var sharedSessionManager: AFHTTPSessionManager?
  
  /// Struct containing String constants only used by this class
  private struct NetworkingConstants {
    private struct Certificate {
      static let Name = "ca-cert"
      static let Type = "cer"
    }
  }
  
  /// This function returns the shared session manager used within the whole system. The object is lazily created
  class func instance() -> AFHTTPSessionManager? {
    if sharedSessionManager == nil {
      logger.debug("Creating new session instance")
      if let baseUrlString = MVSecurity.instance().currentKeychain().domain,
        baseUrl = NSURL(string: baseUrlString),
        certificatePath = NSBundle.mainBundle().pathForResource(NetworkingConstants.Certificate.Name, ofType: NetworkingConstants.Certificate.Type),
        certificate = NSData(contentsOfFile: certificatePath)
      {
        sharedSessionManager = AFHTTPSessionManager(baseURL: baseUrl)
        sharedSessionManager?.securityPolicy = AFSecurityPolicy(pinningMode: .Certificate)
        
        sharedSessionManager?.securityPolicy.allowInvalidCertificates = true
        sharedSessionManager?.securityPolicy.pinnedCertificates = [certificate]
        
        sharedSessionManager?.responseSerializer = AFJSONResponseSerializer()
        
        logger.info("Successfully created new session instance")
      } else {
        logger.warning("Unable to create new session instance")
      }
    }
    return sharedSessionManager
  }
  
  /// This function invalidates the current instance of the session manager
  class func invalidateInstance() {
    logger.debug("Invalidating session instance")
    sharedSessionManager?.invalidateSessionCancelingTasks(true)
    sharedSessionManager = nil
  }
}