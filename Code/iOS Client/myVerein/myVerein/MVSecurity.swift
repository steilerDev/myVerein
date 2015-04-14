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
//  MVSecurity.swift
//  This file holds all information related with parsing and retrieving a division.
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
  
  /// This function either creates a new security instance or returns the shared one.
  class func instance() -> MVSecurity {
    if let currentSecurity = security {
      return currentSecurity
    } else {
      security = MVSecurity()
      return security!
    }
  }
  
  /// This initializer gets the current credentials from the keychain. The shared instance is always in sync with the keychain.
  init() {
    logger.verbose("Initializing security instance")
    if let keychainData = Locksmith.loadDataForUserAccount(SecurityConstants.UserAccount).0 {
      currentUsername = keychainData[SecurityConstants.Username] as? String
      currentPassword = keychainData[SecurityConstants.Password] as? String
      currentDomain = keychainData[SecurityConstants.Domain] as? String
      logger.debug("Successfully initialized security instance")
    } else {
      let error = MVError.createError(.MVKeychainEmptyError)
      logger.warning("Unable to initialize security instance: \(error.extendedDescription)")
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
      currentDomain = newDomain
    } else {
      logger.info("Deleting current keychain object")
      Locksmith.deleteDataForUserAccount(SecurityConstants.UserAccount)
      currentUsername = nil
      currentPassword = nil
      currentUsername = nil
    }
  }
  
  /// This function returns the current state of the keychain. If a property is not set nil is returned.
  func currentKeychain() -> (username: String?, password: String?, domain: String?) {
    logger.debug("Returning current keychain credentials")
    return (currentUsername, currentPassword, currentDomain)
  }
}