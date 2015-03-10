//
//  MarshalOperator.swift
//  myVerein
//
//  Created by Frank Steiler on 10/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation

// This file introduces a new infix operator: '~>'. It will be called the marshal operator. It defines the execution of two closures after each other on different threads. The operator is implemented according to Josh Smith's suggestion: http://ijoshsmith.com/2014/07/05/custom-threading-operator-in-swift/

// The operator can be used infix, prefix and postfix, where infix defines a background task followed by a main queue task, prefix defines a main queue tast and postfix defines a background task. Therefore the operator handles all threading tasks.

infix operator ~> {}
prefix operator ~> {}
postfix operator ~> {}

// Serial dispatched queue used by ~>
private let queue = dispatch_queue_create("serial-worker", DISPATCH_QUEUE_SERIAL)

/// Using the Marshal operator as a prefix operator, means that the closure is executed on the main thread.
/// :param: mainClosure The closure executed on the main thread.
prefix func ~> (mainClosure: () -> ()) {
    dispatch_async(dispatch_get_main_queue(), mainClosure)
}

/// Using the Marshal operator as a postfix operator, means that the closure is executed on a background thread.
/// :param: backgroundClosure The closure executed on a background thread.
postfix func ~> (backgroundClosure: () -> ()) {
    dispatch_async(queue, backgroundClosure)
}

/// Executes the left-hand closure on the background thread, upon completion the right-hand closure is executed on the main thread.
/// :param: backgroundClosure The closure executed on a background thread.
/// :param: mainClosure The closure executed on the main thread, after the background thread is finished.
func ~> (backgroundClosure: () -> (), mainClosure: () -> ()) {
    dispatch_async(queue) {
        backgroundClosure()
        dispatch_async(dispatch_get_main_queue(), mainClosure)
    }
}

/// Executes the left-hand closure on the background thread, upon completion the right-hand closure is executed on the main thread using the return value of the left-hand closure.
/// :param: backgroundClosure The closure executed on a background thread.
/// :param: mainClosure The closure executed on the main thread.
func ~> <R> (backgroundClosure: () -> (R), mainClosure: (R) -> ()) {
    dispatch_async(queue) {
        let result = backgroundClosure()
        dispatch_async(dispatch_get_main_queue()) {
            mainClosure(result)
        }
    }
}