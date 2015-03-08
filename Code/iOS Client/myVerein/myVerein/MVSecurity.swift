//
//  MVSecurity.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import Locksmith

class MVSecurity {
    
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
        if let keychainData = Locksmith.loadDataForUserAccount(SecurityConstants.UserAccount).0 {
            currentUsername = keychainData[SecurityConstants.Username] as? String
            currentPassword = keychainData[SecurityConstants.Password] as? String
            currentDomain = keychainData[SecurityConstants.Domain] as? String
        } else {
            println(MVError.createError(.MVKeychainEmptyError))
        }
    }
    
    /// This function updates the stored information within the keychain. If one of the arguments is nil, the entry is deleted.
    func updateKeychain(newUsername: String?, newPassword: String?, newDomain: String?) {
        if let username = newUsername, password = newPassword, domain = newDomain {
            if currentUsername == nil && currentDomain == nil && currentPassword == nil {
                Locksmith.saveData([SecurityConstants.Username: username, SecurityConstants.Password: password, SecurityConstants.Domain: domain], forUserAccount: SecurityConstants.UserAccount)
            } else {
                Locksmith.updateData([SecurityConstants.Username: username, SecurityConstants.Password: password, SecurityConstants.Domain: domain], forUserAccount: SecurityConstants.UserAccount)
            }
            currentUsername = newUsername
            currentPassword = newPassword
            currentUsername = newDomain
        } else {
            Locksmith.deleteDataForUserAccount(SecurityConstants.UserAccount)
            currentUsername = nil
            currentPassword = nil
            currentUsername = nil
        }
    }
    
    func currentKeychain() -> (username: String?, password: String?, domain: String?) {
        return (currentUsername, currentPassword, currentDomain)
    }
}