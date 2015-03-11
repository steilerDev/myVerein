//
//  TimestampParser.swift
//  myVerein
//
//  Created by Frank Steiler on 08/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import XCGLogger

class DateParser {
    
    private static let logger = XCGLogger.defaultInstance()
    
    struct TimestampConstants {
        static let DayOfMonth = "dayOfMonth"
        static let Month = "monthValue"
        static let Year = "year"
        static let Hour = "hour"
        static let Minute = "minute"
        static let Second = "second"
        static let NanoSecond = "nano"
    }
    
    /// This function parse the response object of a (Java 8) LocalDateTime to a NSDate. The object is nil if a parse error occured.
    class func parseDateTime(responseObject: [String: AnyObject]?) -> NSDate? {
        
        logger.verbose("Parsing DateTime: \(responseObject)")
        
        var dateComponent = NSDateComponents()
        
        if let response = responseObject,
                dayOfMonth = response[TimestampConstants.DayOfMonth] as? Int,
                month = response[TimestampConstants.Month] as? Int,
                year = response[TimestampConstants.Year] as? Int,
                hour = response[TimestampConstants.Hour] as? Int,
                minute = response[TimestampConstants.Minute] as? Int,
                second = response[TimestampConstants.Second] as? Int,
                nanoSecond = response[TimestampConstants.NanoSecond] as? Int
        {
            dateComponent.day = dayOfMonth
            dateComponent.month = month
            dateComponent.year = year
            dateComponent.hour = hour
            dateComponent.minute = minute
            dateComponent.second = second
            dateComponent.nanosecond = nanoSecond
            logger.info("Successfully parsed DateTime data")
            return NSCalendar.currentCalendar().dateFromComponents(dateComponent)
        } else {
            logger.warning("Unable to parse DateTime data: \(responseObject)")
            return nil
        }
    }
    
    /// This function parse the response object of a (Java 8) LocalDate to a NSDate. The object is nil if a parse error occured.
    class func parseDate(responseObject: [String: AnyObject]?) -> NSDate? {
        
        logger.verbose("Parsing Date: \(responseObject)")
        
        var dateComponent = NSDateComponents()
        
        if let response = responseObject,
            dayOfMonth = response[TimestampConstants.DayOfMonth] as? Int,
            month = response[TimestampConstants.Month] as? Int,
            year = response[TimestampConstants.Year] as? Int
        {
            dateComponent.day = dayOfMonth
            dateComponent.month = month
            dateComponent.year = year
            logger.info("Successfully parsed Date data")
            return NSCalendar.currentCalendar().dateFromComponents(dateComponent)
        } else {
            logger.warning("Unable to parse Date data: \(responseObject)")
            return nil
        }
    }
}