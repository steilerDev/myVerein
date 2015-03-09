//
//  MVSecurity.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import Locksmith
import XCGLogger

class MVSecurity {
    
    private let logger = XCGLogger.defaultInstance()
    
    private struct SecurityConstants {
        static let UserAccount = "myVereinUserAccount"
        static let Username = "myVereinUsername"
        static let Password = "myVereinPassword"
        static let Domain = "myVereinDomain"
    }
    
    private static var security: MVSecurity?
    
    private var currentUsername: String?
    private var currentPassword: String?
    private var currentDomain: String?
    
    class func instance() -> MVSecurity {
        if let currentSecurity = security {
            return currentSecurity
        } else {
            security = MVSecurity()
            return security!
        }
    }
    
    init() {
        logger.verbose("Initializing security instance")
        if let keychainData = Locksmith.loadDataForUserAccount(SecurityConstants.UserAccount).0 {
            currentUsername = keychainData[SecurityConstants.Username] as? String
            currentPassword = keychainData[SecurityConstants.Password] as? String
            currentDomain = keychainData[SecurityConstants.Domain] as? String
            logger.debug("Successfully initialized security instance")
        } else {
            let error = MVError.createError(.MVKeychainEmptyError)
            logger.warning("Unable to initialize security instance: \(error.localizedDescription)")
        }
    }
    
    /// This function updates the stored information within the keychain. If one of the arguments is nil, the entry is deleted.
    func updateKeychain(newUsername: String?, newPassword: String?, newDomain: String?) {
        logger.verbose("Updating keychain information")
        if let username = newUsername, password = newPassword, domain = newDomain {
            if currentUsername == nil && currentDomain == nil && currentPassword == nil {
                logger.info("Creating new keychain object")
                Locksmith.saveData([SecurityConstants.Username: username, SecurityConstants.Password: password, SecurityConstants.Domain: domain], forUserAccount: SecurityConstants.UserAccount)
            } else {
                logger.info("Updating keychain object")
                Locksmith.updateData([SecurityConstants.Username: username, SecurityConstants.Password: password, SecurityConstants.Domain: domain], forUserAccount: SecurityConstants.UserAccount)
            }
            currentUsername = newUsername
            currentPassword = newPassword
            currentUsername = newDomain
        } else {
            logger.info("Deleting current keychain object")
            Locksmith.deleteDataForUserAccount(SecurityConstants.UserAccount)
            currentUsername = nil
            currentPassword = nil
            currentUsername = nil
        }
    }
    
    func currentKeychain() -> (username: String?, password: String?, domain: String?) {
        logger.debug("Returning current keychain credentials")
        return (currentUsername, currentPassword, currentDomain)
    }
}