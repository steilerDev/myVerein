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

    private struct DivisionConstants {
        static let ChatMessages = "chatMessages"
        static let EnrolledUser = "enrolledUser"
    }
    
    @NSManaged var desc: String
    @NSManaged var id: String
    @NSManaged var name: String
    @NSManaged var lastSynced: NSDate
    @NSManaged var userIsMember: Bool
    
    
    @NSManaged var rawChatMessages: NSSet
    var chatMessages: NSMutableSet {
        return mutableSetValueForKey(DivisionConstants.ChatMessages)
    }
    
    @NSManaged private var rawEnrolledUser: NSSet
    var enrolledUser: NSMutableSet {
        return mutableSetValueForKey(DivisionConstants.EnrolledUser)
    }
    
    /// This function adds a user from to the list of enrolled user and adds the inverse connection. Note: You do not need to call addDivision on the user, since the inverse connection is allready taken care of.
    func addEnrolledUser(user: User) {
        enrolledUser.addObject(user)
        user.divisions.addObject(self)
    }
    
    /// This function removes a user from the list of enrolled user and removes the inverse connection. Note: You do not need to call removeDivision on the user, since the inverse connection is allready taken care of.
    func removeEnrolledUser(user: User) {
        enrolledUser.removeObject(user)
        user.divisions.removeObject(self)
    }
    
    @NSManaged var admin: User?
    func setAdminUser(admin: User) {
        self.admin?.administratedDivisions.removeObject(self)
        self.admin = admin
        admin.administratedDivisions.addObject(self)
    }
}
