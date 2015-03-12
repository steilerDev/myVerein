//
//  Division.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData

class Division: NSManagedObject, MVCoreDataObject {

    private struct DivisionConstants {
        static let ChatMessages = "rawChatMessage"
        static let EnrolledUser = "rawEnrolledUser"
    }
    
    enum UserMembershipStatus: String {
        case Member = "MEMBER"
        case FormerMember = "FORMERMEMBER"
        case NoMember = "NOMEMBER"
    }
    
    static var syncRequired: MVCoreDataObject -> Bool = {
        object in
        if let division = object as? Division {
            return division.name == nil
        } else {
            return false
        }
    }
    
    static var syncFunction: MVCoreDataObject -> () = {
        object in
        if let division = object as? Division {
            MVNetworkingHelper.syncDivision(division.id)
        }
    }
    
    @NSManaged var id: String
    @NSManaged var desc: String?
    @NSManaged var name: String?
    @NSManaged var lastSynced: NSDate
    
    @NSManaged private var rawUserMembershipStatus: String
    var userMembershipStatus: UserMembershipStatus {
        get {
            return UserMembershipStatus(rawValue: rawUserMembershipStatus) ?? .NoMember
        }
        set {
            rawUserMembershipStatus = newValue.rawValue
        }
    }
    
    @NSManaged var rawChatMessage: NSSet?
    
    @NSManaged var rawEnrolledUser: NSSet?
    
    @NSManaged var admin: User?
}
