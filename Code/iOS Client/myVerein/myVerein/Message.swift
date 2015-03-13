//
//  Message.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData

class Message: NSManagedObject, MVCoreDataObject {
    
    @NSManaged var content: String?
    @NSManaged var id: String
    @NSManaged var read: Bool
    @NSManaged var timestamp: NSDate?
    @NSManaged var division: Division
    @NSManaged var sender: User
    
    // MARK: - MVCoreDataObject
    
    var syncRequired: Bool {
        return false
    }
    
    func sync() {
        MVNetworkingHelper.syncMessages()
    }
}
