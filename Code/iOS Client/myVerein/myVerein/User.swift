//
//  User.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import CoreData

class User: NSManagedObject {

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
    
    @NSManaged var birthday: NSDate
    @NSManaged var city: String
    @NSManaged var country: String
    @NSManaged var email: String
    @NSManaged var firstName: String
    @NSManaged var id: String
    @NSManaged var lastName: String
    @NSManaged var street: String
    @NSManaged var streetNumber: String
    @NSManaged var zipCode: String
    @NSManaged var lastSynced: NSDate
    
    
    @NSManaged private var rawMembershipStatus: String
    var membershipStatus: MembershipStatus? {
        get {
            return MembershipStatus(rawValue: rawMembershipStatus)
        }
        set {
            rawMembershipStatus = newValue?.rawValue ?? ""
        }
    }
    
    @NSManaged private var rawGender: String
    var gender: Gender? {
        get {
            return Gender(rawValue: rawGender)
        }
        set {
            rawGender = newValue?.rawValue ?? ""
        }
    }
    
    @NSManaged private var rawAdministratedDivisions: NSSet
    var administratedDivisions: NSMutableSet {
        return mutableSetValueForKey(UserConstants.AdministratedDivisions)
    }
    
    func addAdministratedDivision(division: Division) {
        administratedDivisions.addObject(division)
        division.admin = self
    }
    
    func removeAdministratedDivision(division: Division) {
        administratedDivisions.removeObject(division)
        division.admin = self
    }
    
    @NSManaged private var rawDivisions: NSSet
    var divisions: NSMutableSet {
        return mutableSetValueForKey(UserConstants.SendMessages)
    }
    
    /// This function adds a division to the list of divisions, the user is part of and adds the inverse connection. Note: You do not need to call addEnrolledUser on the division, since the inverse connection is allready taken care of.
    func addDivision(division: Division) {
        divisions.addObject(division)
        division.enrolledUser.addObject(self)
    }
    
    // This function removes a division from the list of divisions and removes the inverse connection. Note: You do not need to call removeEnrolledUser on the division, since the inverse connection is allready taken care of.
    func removeDivision(division: Division) {
        divisions.removeObject(division)
        division.enrolledUser.removeObject(self)
    }
    
    @NSManaged private var rawSendMessages: NSSet
    var sendMessages: NSMutableSet {
        return mutableSetValueForKey(UserConstants.SendMessages)
    }
}
