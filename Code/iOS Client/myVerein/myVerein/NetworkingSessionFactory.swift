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

class NetworkingSessionFactory {
    
    private static var sharedSessionManager: AFHTTPSessionManager?
    
    /// Struct containing String constants only used by this class
    private struct NetworkingConstants {
        private struct Certificate {
            static let Name = "ca-cert"
            static let Type = "cer"
        }
    }
    
    /// This function returns the shared session manager used within the whole system. The object is lazily created
    static func instance() -> AFHTTPSessionManager? {
        if sharedSessionManager == nil {
            if let baseUrlString = (Locksmith.loadDataForUserAccount(GlobalConstants.Keychain.UserAccount).0 as? [String: String])?[GlobalConstants.Keychain.Domain],
                baseUrl = NSURL(string: baseUrlString),
                certificatePath = NSBundle.mainBundle().pathForResource(NetworkingConstants.Certificate.Name, ofType: NetworkingConstants.Certificate.Type),
                certificate = NSData(contentsOfFile: certificatePath)
            {
                sharedSessionManager = AFHTTPSessionManager(baseURL: baseUrl)
                sharedSessionManager?.securityPolicy = AFSecurityPolicy(pinningMode: .Certificate)
                
                sharedSessionManager?.securityPolicy.allowInvalidCertificates = true
                sharedSessionManager?.securityPolicy.pinnedCertificates = [certificate]
            }
        }
        return sharedSessionManager
    }
    
    /// This function invalidates the current instance of the session manager
    static func invalidateInstance() {
        sharedSessionManager = nil
    }
}