//
//  TimestampParser.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

class TimestampParser {
    struct TimestampConstants {
        static let DayOfMonth = "dayOfMonth"
        static let Month = "monthValue"
        static let Year = "year"
        static let Hour = "hour"
        static let Minute = "minute"
        static let Second = "second"
        static let NanoSecond = "nano"
    }
    
    /// This function parse the response object of a timestamp to a NSDate. The object is nil if a parse error occured.
    class func parseTimestamp(responseObject: Dictionary<String, AnyObject>) -> NSDate? {
        var dateComponent = NSDateComponents()
        
        if let dayOfMonth = responseObject[TimestampConstants.DayOfMonth] as? Int,
                month = responseObject[TimestampConstants.Month] as? Int,
                year = responseObject[TimestampConstants.Year] as? Int,
                hour = responseObject[TimestampConstants.Hour] as? Int,
                minute = responseObject[TimestampConstants.Minute] as? Int,
                second = responseObject[TimestampConstants.Second] as? Int,
                nanoSecond = responseObject[TimestampConstants.NanoSecond] as? Int
        {
            dateComponent.day = dayOfMonth
            dateComponent.month = month
            dateComponent.year = year
            dateComponent.hour = hour
            dateComponent.minute = minute
            dateComponent.second = second
            dateComponent.nanosecond = nanoSecond
            return NSCalendar.currentCalendar().dateFromComponents(dateComponent)
        } else {
            println("Unable to parse timestamp data")
            return nil
        }
    }
}