//
//  Message.swift
//  myVerein
//
//  Created by Frank Steiler on 05/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData

class Message: NSManagedObject {

    @NSManaged var content: String
    @NSManaged var timestamp: NSDate
    @NSManaged var read: NSNumber

    
    
}
