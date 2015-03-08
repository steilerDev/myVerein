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

    @NSManaged var birthday: NSTimeInterval
    @NSManaged var city: String
    @NSManaged var country: String
    @NSManaged var email: String
    @NSManaged var firstName: String
    @NSManaged var gender: String
    @NSManaged var id: String
    @NSManaged var lastName: String
    @NSManaged var membershipStatuts: String
    @NSManaged var street: String
    @NSManaged var streetNumber: String
    @NSManaged var zipCode: String
    @NSManaged var division: NSSet
    @NSManaged var administratedDivision: NSSet
    @NSManaged var sendMessages: NSSet

}
