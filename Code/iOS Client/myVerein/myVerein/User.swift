//
//  User.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData

class User: NSManagedObject, MVCoreDataObject {

    private struct UserConstants {
        static let SendMessages = "rawSendMessages"
        static let Divisions = "rawDivisions"
        static let AdministratedDivisions = "rawAdministratedDivisions"
    }
    
    enum Gender: String {
        case Male = "MALE"
        case Female = "FEMALE"
    }
    
    enum MembershipStatus: String {
        case Active = "ACTIVE"
        case Passive = "PASSIVE"
        case Resigned = "RESIGNED"
    }
    
    @NSManaged var birthday: NSDate?
    @NSManaged var city: String?
    @NSManaged var country: String?
    @NSManaged var email: String?
    @NSManaged var firstName: String?
    @NSManaged var id: String
    @NSManaged var lastName: String?
    @NSManaged var street: String?
    @NSManaged var streetNumber: String?
    @NSManaged var zipCode: String?
    @NSManaged var lastSynced: NSDate
    
    @NSManaged var rawMembershipStatus: String?
    var membershipStatus: MembershipStatus? {
        get {
            if rawMembershipStatus != nil {
                return MembershipStatus(rawValue: rawMembershipStatus!)
            } else {
                return nil
            }
        }
        set {
            rawMembershipStatus = newValue?.rawValue
        }
    }
    
    @NSManaged var rawGender: String?
    var gender: Gender? {
        get {
            if rawGender != nil {
                return Gender(rawValue: rawGender!)
            } else {
                return nil
            }
        }
        set {
            rawGender = newValue?.rawValue
        }
    }
    
    @NSManaged private var rawAdministratedDivisions: NSSet
    var administratedDivisions: NSMutableSet {
        return mutableSetValueForKey(UserConstants.AdministratedDivisions)
    }
    
    @NSManaged private var rawDivisions: NSSet
    var divisions: NSMutableSet {
        return mutableSetValueForKey(UserConstants.Divisions)
    }
    
    @NSManaged private var rawSendMessages: NSSet
    var sendMessages: NSMutableSet {
        return mutableSetValueForKey(UserConstants.SendMessages)
    }
    
    // MARK: - MVCoreDataObject
    
    var syncRequired: Bool {
        return (email == nil || firstName == nil || lastName == nil)
    }
    
    func sync() {
        MVNetworkingHelper.syncUser(id)
    }
}
