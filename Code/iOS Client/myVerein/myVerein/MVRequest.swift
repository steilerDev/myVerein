//
//  MVRequest.swift
//  myVerein
//
//  Created by Frank Steiler on 18/04/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import Foundation
import AFNetworking

/// This struct represents the state of a single request containing all necessary information to (re-)execute the request at any given time.
struct MVRequest: Printable {
  let URI: String
  let parameters: [String: String]?
  let requestMethod: HTTPMethods
  let success: ((AnyObject) -> ())?
  let failure: ((NSError) -> ())?
  
  init(URI: String, parameters: [String: String]?, requestMethod: HTTPMethods, success: ((AnyObject) -> ())?, failure: ((NSError) -> ())?) {
    self.URI = URI
    self.parameters = parameters
    self.requestMethod = requestMethod
    self.success = success
    self.failure = failure
  }
  
  func requestFunctionInSession(session: AFHTTPSessionManager) -> ((String!, parameters: AnyObject!, success: ((NSURLSessionDataTask!, AnyObject!) -> ())!, failure: ((NSURLSessionDataTask!, NSError!) -> ())!) -> NSURLSessionDataTask!) {
    switch requestMethod {
    case .DELETE:
      return session.DELETE
    case .GET:
      return session.GET
    case .POST:
      return session.POST
    }
  }
  
  var description: String {
    return "MVRequest (URI \(URI); parameters \(parameters); request method \(requestMethod))"
  }
}