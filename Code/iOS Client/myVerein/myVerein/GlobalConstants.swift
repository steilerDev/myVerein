//
//  API_URI.swift
//  myVerein
//
//  Created by Frank Steiler on 26/02/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

/// Struct containing String constants used by the whole project
struct GlobalConstants {
    
    struct Keychain {
        static let UserAccount = "myVereinUserAccount"
        static let Username = "myVereinUsername"
        static let Password = "myVereinPassword"
        static let Domain = "myVereinDomain"
    }
    
    struct API {
        static let Login = "/api/login"
    }
}