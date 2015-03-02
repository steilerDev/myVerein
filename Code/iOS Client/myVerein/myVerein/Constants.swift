//
//  API_URI.swift
//  myVerein
//
//  Created by Frank Steiler on 26/02/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

/// This struct is holding the information about the URIs of the appliation
struct Constants {
    
    static let userAccount = "myVereinUserAccount"
    static let keychainUsernameField = "myVereinUsername"
    static let keychainPasswordField = "myVereinPassword"
    static let keychainDomainField = "myVereinDomain"
    
    struct API {
        static let login = "/api/login"
    }
}