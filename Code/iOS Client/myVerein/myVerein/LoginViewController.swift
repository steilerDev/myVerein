//
//  ViewController.swift
//  myVerein
//
//  Created by Frank Steiler on 25/02/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit
import pop
import AFNetworking
import SwiftyUserDefaults
import Locksmith
import OnePasswordExtension
import XCGLogger

import CoreData

// TODO: Convert to XIB

class LoginViewController: UIViewController {
  
  static let StoryBoardID = "LoginViewControllerID"
  
  /// Struct containing String constants only used by this class
  private struct LoginViewControllerConstants {
    static let LoginBoxAnimationKey = "moveLoginBoxAnimation"
    static let WrongPasswordAnimationKey = "shakePassword"
    
    static let PasswordManagerURL = "https://agilebits.com/onepassword"
  }
  
  private let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var loginBox: UIView!
  @IBOutlet weak var usernameTextField: UITextField!
  @IBOutlet weak var passwordTextField: UITextField!
  @IBOutlet weak var hostTextField: UITextField!
  @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
  @IBOutlet weak var topConstraintForLoginBox: NSLayoutConstraint!
  @IBOutlet weak var loginButton: UIButton!
  
  private var initialLoginPosition: CGPoint?
  
  private lazy var newVariableTopConstraint: NSLayoutConstraint = {
    return NSLayoutConstraint(item: self.loginBox, attribute: .CenterY, relatedBy: .Equal, toItem: self.view, attribute: .Top, multiplier: 1, constant: 0)
  }()
  
  // MARK: - Password management extension
  @IBAction func openPasswordManager(sender: UIButton) {
    logger.verbose("Opening password manager extension")
    if OnePasswordExtension.sharedExtension().isAppExtensionAvailable() {
      let urlString: String
      if hostTextField.text != nil && !hostTextField.text.isEmpty{
        urlString = hostTextField.text
      } else {
        urlString = "myVerein.app"
      }
      OnePasswordExtension.sharedExtension().findLoginForURLString(urlString, forViewController: self, sender: sender)
        {
          loginDict, error in
          let logger = XCGLogger.defaultInstance()
          if let login = loginDict as? [String: String] {
            if let username = login[AppExtensionUsernameKey], password = login[AppExtensionPasswordKey], domain = login[AppExtensionURLStringKey] {
              logger.debug("Filling fields with stored information")
              self.usernameTextField.text = username
              self.passwordTextField.text = password
              self.hostTextField.text = domain
            } else {
              logger.warning("Unable to get required fields from password manager")
            }
          } else {
            if (error.code != Int(AppExtensionErrorCodeCancelledByUser)) {
              logger.warning("Unable to load credentials from password manager: \(error.localizedDescription)")
            } else {
              logger.info("Retrieving credentials from password manager cancelled by user: \(error.localizedDescription)")
            }
          }
      }
    } else {
      var alert = UIAlertController(title: "No password manager found", message: "To use this extension you need to install a password manager, supporting iOS 8 extensions, like 1Password", preferredStyle: .Alert)
      alert.addAction(UIAlertAction(title: "Okay", style: .Cancel, handler: { (action) -> Void in }))
      alert.addAction(UIAlertAction(title: "Learn more", style: .Default, handler: { (action) -> Void in
        UIApplication.sharedApplication().openURL(NSURL(string: LoginViewControllerConstants.PasswordManagerURL)!)
      }))
      presentViewController(alert, animated: true, completion: nil)
      logger.info("Unable to find any password manager")
    }
  }
  
  // MARK: - Log in
  
  /// This function stores the current text fields within the keychain and then validates them using the validate login method
  @IBAction func loginAction() {
    logger.verbose("Trying to log in user")
    if var hostname = hostTextField.text, username = usernameTextField.text, password = passwordTextField.text
      where !hostname.isEmpty && !username.isEmpty && !password.isEmpty
    {
      //Adjusting hostname to meet required format
      if !hostname.hasPrefix("http") {
        hostname = "http://" + hostname
      }
      if !hostname.hasSuffix("/") {
        hostname += "/"
      }
      
      MVSecurity.instance().updateKeychain(username, newPassword: password, newDomain: hostname)
      validateCurrentLogin()
    } else {
      logger.warning("Unable to get values from text fields")
      animateInvalidLogin()
    }
  }
  
  // This function uses the keychain to try and log into the system
  func validateCurrentLogin() {
    logger.verbose("Validating login stored within keychain")
    activityIndicator.startAnimating()
    dismissKeyboard()
    usernameTextField.enabled = false
    passwordTextField.enabled = false
    hostTextField.enabled = false
    loginButton.enabled = false
    
    let (currentUsername, currentPassword, currentDomain) = MVSecurity.instance().currentKeychain()
    
    if let username = currentUsername,
      password = currentPassword,
      domain = currentDomain {
        usernameTextField.text = username
        passwordTextField.text = password
        hostTextField.text = domain
        
        MVNetworking.loginActionWithCallbackOnMainQueue(
          success: {
            XCGLogger.info("Login successfully, performing segue to main view controller")
            self.dismissViewControllerAnimated(true, completion: {})
            self.activityIndicator.stopAnimating()
          },
          failure:
          {
            error in
            XCGLogger.error("Login was unsuccessfully: \(error.localizedDescription)")
            self.animateInvalidLogin()
          }
        )
    } else {
      let error = MVError.createError(MVErrorCodes.MVKeychainEmptyError)
      logger.error("Unable to perform login: \(error.localizedDescription)")
      animateInvalidLogin()
      
      usernameTextField.text = nil
      passwordTextField.text = nil
      hostTextField.text = nil
    }
  }
  
  private func animateInvalidLogin() {
    logger.verbose("Animating invalid login")
    usernameTextField.enabled = true
    passwordTextField.enabled = true
    hostTextField.enabled = true
    loginButton.enabled = true
    
    // Create animation in case of log-in failure
    let shake = POPSpringAnimation(propertyNamed: kPOPLayerPositionX)
    shake.springBounciness = 20
    shake.velocity = 3000
    
    
    if(usernameTextField.isFirstResponder() || passwordTextField.isFirstResponder() || hostTextField.isFirstResponder()) {
      logger.debug("Shaking text field")
      loginBox.pop_addAnimation(shake, forKey: LoginViewControllerConstants.WrongPasswordAnimationKey)
    } else {
      usernameTextField.becomeFirstResponder()
    }
    activityIndicator.stopAnimating()
  }
  
  // MARK: - Text field animation and keyboard management
  
  @IBAction func recogniseTap(sender: UITapGestureRecognizer) {
    dismissKeyboard()
  }
  
  /// This function is invoked, as soon as a touch outside the field was recognized. The keyboard is dismissed by calling resignFirstResponder on the currently edited textfield.
  func dismissKeyboard() {
    logger.verbose("Dismissing keyboard")
    if usernameTextField.editing {
      usernameTextField.resignFirstResponder()
    } else if passwordTextField.editing {
      passwordTextField.resignFirstResponder()
    } else if hostTextField.editing {
      hostTextField.resignFirstResponder()
    }
  }
  
  /// This function is invoked when the notification center fires the keyboard did show notification
  func moveLoginBoxUp(notification: NSNotification) {
    var computedLoginPosition: CGFloat
    if let keyboardSize = notification.userInfo?[UIKeyboardFrameEndUserInfoKey]?.CGRectValue().size.height where (view.frame.size.height - keyboardSize) >= loginBox.frame.size.height {
      computedLoginPosition = (view.frame.size.height - keyboardSize)/2
    } else {
      computedLoginPosition = (loginBox.frame.size.height/2) + 24
    }
    logger.debug("Moving login box up to \(computedLoginPosition)")
    moveLoginBox(computedLoginPosition)
  }
  
  /// This function is invoked when the notification center fires the keyboard did hide notification
  func moveLoginBoxDown() {
    logger.debug("Moving login box down")
    moveLoginBox(nil)
  }
  
  /// This function is moving the login box to the parameter value using a pop spring animation
  private func moveLoginBox(toValue: CGFloat?) {
    logger.verbose("Moving login box to \(toValue)")
    view.removeConstraint(topConstraintForLoginBox)
    
    var newCenter = CGPoint()
    newCenter.x = view.center.x
    
    newCenter.y = toValue ?? initialLoginPosition?.y ?? (loginBox.frame.size.height/2) + 24 //Use either the to value or the initial value, default should never be used
    
    logger.debug("New login box center: \(newCenter)")
    
    var moveAnimation = POPSpringAnimation(propertyNamed: kPOPViewCenter)
    moveAnimation.springBounciness = 10
    moveAnimation.springSpeed = 10
    moveAnimation.toValue = NSValue(CGPoint: newCenter)
    moveAnimation.delegate = self
    moveAnimation.name = LoginViewControllerConstants.LoginBoxAnimationKey
    loginBox.pop_addAnimation(moveAnimation, forKey: LoginViewControllerConstants.LoginBoxAnimationKey)
  }
  
  // MARK: - ViewController Lifecycle
  
  /// The function registers gesture recognizer dismissing the keyboard
  override func viewDidLoad() {
    logger.verbose("Login view did load")
    super.viewDidLoad()
    NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardWillShowNotification, object: nil)
    NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardDidShowNotification, object: nil)
    NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardDidHideNotification, object: nil)
    NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardWillHideNotification, object: nil)
    initialLoginPosition = loginBox.center
    validateCurrentLogin()
  }
}

// MARK: - POPAnimationDelegate
extension LoginViewController: POPAnimationDelegate {
  func pop_animationDidReachToValue(anim: POPAnimation!) {
    logger.verbose("Pop animation finished, resetting constraints")
    view.removeConstraint(newVariableTopConstraint)
    newVariableTopConstraint.constant = loginBox.center.y
    view.addConstraint(newVariableTopConstraint)
  }
}

// MARK: - UITextFieldDelegate
extension LoginViewController: UITextFieldDelegate {
  
  func textFieldShouldReturn(textField: UITextField) -> Bool {
    logger.verbose("Detected enter press on UITextField")
    switch textField {
    case usernameTextField:
      passwordTextField.becomeFirstResponder()
    case passwordTextField:
      hostTextField.becomeFirstResponder()
    case hostTextField:
      loginAction()
    default: break
    }
    return false
  }
}
