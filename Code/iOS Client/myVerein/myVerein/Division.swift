//
//  Division.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData

class Division: NSManagedObject {

    @NSManaged var desc: String
    @NSManaged var id: String
    @NSManaged var name: String
    @NSManaged var lastSynced: NSDate
    @NSManaged var admin: User
    @NSManaged var chatMessages: NSSet
    @NSManaged var enrolledUser: NSSet
    @NSManaged var userIsMember: Bool
}
