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
//  NetworkingSessionFactory.swift
//  This class is handling the shared session object, and its configuration.
//

import Foundation

import AFNetworking
import Locksmith
import XCGLogger

/// This class is used to manage the shared HTTPSession.
class MVNetworkingSessionFactory {
  
  private static let logger = XCGLogger.defaultInstance()
  
  static var instance: AFHTTPSessionManager {
    get {
      if _instance == nil {
        _instance = MVNetworkingSessionFactory.createInstance()
      }
      return _instance!
    }
  }
  
  private static var _instance: AFHTTPSessionManager?
  
  /// Struct containing String constants only used by this class
  private struct NetworkingConstants {
    private struct Certificate {
      static let Name = "ca-cert"
      static let Type = "cer"
    }
  }
  
  /// This function returns the shared session manager used within the whole system. The object is lazily created
  class func createInstance() -> AFHTTPSessionManager? {
    logger.debug("Creating new session instance")
    
    let baseUrlString = MVSecurity.instance.currentKeychain().domain
    
    if let baseUrl = NSURL(string: baseUrlString) ,
      certificatePath = NSBundle.mainBundle().pathForResource(NetworkingConstants.Certificate.Name, ofType: NetworkingConstants.Certificate.Type),
      certificate = NSData(contentsOfFile: certificatePath) {
      let sharedSessionManager = AFHTTPSessionManager(baseURL: baseUrl)
      sharedSessionManager?.securityPolicy = AFSecurityPolicy(pinningMode: .Certificate)
      
      sharedSessionManager?.securityPolicy.allowInvalidCertificates = true
      sharedSessionManager?.securityPolicy.pinnedCertificates = [certificate]
      
      sharedSessionManager?.responseSerializer = AFJSONResponseSerializer()
      
      logger.info("Successfully created new session instance")
      return sharedSessionManager
    } else {
      logger.error("Unable to create new session instance")
      return nil
    }
  }
  
  /// This function invalidates the current instance of the session manager
  class func invalidateInstance() {
    logger.debug("Invalidating session instance")
    if _instance != nil {
      _instance!.invalidateSessionCancelingTasks(true)
      _instance = nil
    }
  }
}