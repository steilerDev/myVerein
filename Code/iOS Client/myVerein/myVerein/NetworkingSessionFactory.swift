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
    
    private static var sharedSession: AFHTTPSessionManager?
    
    /// This
    static func instance() -> AFHTTPSessionManager? {
        if sharedSession == nil {
            if let baseUrlString = (Locksmith.loadDataForUserAccount(Constants.userAccount).0 as? [String: String])?[Constants.keychainDomainField],
                baseUrl = NSURL(string: baseUrlString),
                certificatePath = NSBundle.mainBundle().pathForResource("ca-cert", ofType: "cer"),
                certificate = NSData(contentsOfFile: certificatePath)
            {
                sharedSession = AFHTTPSessionManager(baseURL: baseUrl)
                sharedSession?.securityPolicy = AFSecurityPolicy(pinningMode: .Certificate)
                
                sharedSession?.securityPolicy.allowInvalidCertificates = true
                sharedSession?.securityPolicy.pinnedCertificates = [certificate]
            }
        }
        return sharedSession
    }
    
    static func invalidateInstance() {
        sharedSession = nil
    }
}