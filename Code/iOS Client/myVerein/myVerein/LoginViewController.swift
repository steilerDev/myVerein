//
// Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

//
//  LoginViewController.swift
//  This file holds all information related to the login view. It includes managing the animation and storing the credentials within the user defaults. If the credentials are valid the controller is poping of the view stack.
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
  
  // Variables used to store the notification observer, to be able to remove them as soon as they are no longer needed
  private var keyboardWillShowObserver: NSObjectProtocol?
  private var keyboardDidShowObserver: NSObjectProtocol?
  private var keyboardWillHideObserver: NSObjectProtocol?
  private var keyboardDidHideObserver: NSObjectProtocol?
}

// MARK: - Password management extension
extension LoginViewController {
  @IBAction func openPasswordManager(sender: UIButton) {
    logger.verbose("Opening password manager extension")
    if OnePasswordExtension.sharedExtension().isAppExtensionAvailable() {
      let urlString: String
      if hostTextField.text != nil && !hostTextField.text.isEmpty{
        urlString = hostTextField.text
      } else {
        urlString = "myVerein.app"
      }
      OnePasswordExtension.sharedExtension().findLoginForURLString(urlString, forViewController: self, sender: sender) {
          loginDict, error in
          let logger = XCGLogger.defaultInstance()
          if let loginDict = loginDict as? [String: String] {
            logger.debug("Filling fields with stored information")
            logger.debug("\(loginDict)")
            if let username = loginDict[AppExtensionUsernameKey] {
              self.usernameTextField.text = username
            } else {
              logger.warning("No username available")
            }
            
            if let password = loginDict[AppExtensionPasswordKey] {
              self.passwordTextField.text = password
            } else {
              logger.warning("No password available")
            }
            
            if let domain = loginDict[AppExtensionURLStringKey] {
              self.hostTextField.text = domain
            } else {
              logger.warning("No domain available")
            }
          } else {
            if (error.code != Int(AppExtensionErrorCodeCancelledByUser)) {
              logger.warning("Unable to load credentials from password manager: \(error.extendedDescription)")
            } else {
              logger.info("Retrieving credentials from password manager cancelled by user: \(error.extendedDescription)")
            }
          }
      }
    } else {
      var alert = UIAlertController(title: "No password manager found", message: "To use this extension you need to install a password manager, supporting iOS 8 extensions, like 1Password", preferredStyle: .Alert)
      alert.addAction(UIAlertAction(title: "Okay", style: .Cancel, handler: nil))
      alert.addAction(UIAlertAction(title: "Learn more", style: .Default, handler: { (action) -> Void in
        UIApplication.sharedApplication().openURL(NSURL(string: LoginViewControllerConstants.PasswordManagerURL)!)
      }))
      presentViewController(alert, animated: true, completion: nil)
      logger.info("Unable to find any password manager")
    }
  }
}

// MARK: - Log in functions
extension LoginViewController {
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
      
      MVSecurity.instance.updateKeychain(username, newPassword: password, newDomain: hostname)
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
    
    let (username, password, domain) = MVSecurity.instance.currentKeychain()
    
    if !username.isEmpty &&
      !password.isEmpty &&
      !domain.isEmpty {
        usernameTextField.text = username
        passwordTextField.text = password
        hostTextField.text = domain
        
        MVNetworkingSessionFactory.invalidateInstance()
        
        MVNetworking.instance.performLogIn(showLoginScreenOnFailure: false,
          success: {
            XCGLogger.info("Login successfully, performing segue to main view controller")
            self.dismissViewControllerAnimated(true, completion: {})
            self.activityIndicator.stopAnimating()
          },
          failure:
          {
            error in
            XCGLogger.error("Login was unsuccessfully: \(error.extendedDescription)")
            self.animateInvalidLogin()
          }
        )
    } else {
      let error = MVError.createError(MVErrorCodes.MVKeychainEmptyError)
      logger.error("Unable to perform login: \(error.extendedDescription)")
      animateInvalidLogin()
      
      usernameTextField.text = nil
      passwordTextField.text = nil
      hostTextField.text = nil
    }
  }
}

// MARK: - Keyboard management/tab recognition
extension LoginViewController {
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
}

// MARK: - Login box animation functions
extension LoginViewController {
  /// This function is invoked when the notification center fires the keyboard did show notification
  func moveLoginBoxUp(notification: NSNotification!) {
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
  func moveLoginBoxDown(notification: NSNotification!) {
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
}

// MARK: - ViewController lifecycle methods
extension LoginViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    logger.verbose("Login view did load")
    initialLoginPosition = loginBox.center
    validateCurrentLogin()
  }
  
  override func viewDidAppear(animated: Bool) {
    super.viewDidAppear(animated)
    (UIApplication.sharedApplication().delegate as! AppDelegate).stopBackgroundSync()
    logger.debug("Registering observer for keyboard notification")
    let notificationCenter = NSNotificationCenter.defaultCenter()
    let mainQueue = NSOperationQueue.mainQueue()
    keyboardWillShowObserver = notificationCenter.addObserverForName(UIKeyboardWillShowNotification, object: nil, queue: mainQueue, usingBlock: moveLoginBoxUp)
    keyboardDidShowObserver = notificationCenter.addObserverForName(UIKeyboardDidShowNotification, object: nil, queue: mainQueue, usingBlock: moveLoginBoxUp)
    keyboardWillHideObserver = notificationCenter.addObserverForName(UIKeyboardWillHideNotification, object: nil, queue: mainQueue, usingBlock: moveLoginBoxDown)
    keyboardDidHideObserver = notificationCenter.addObserverForName(UIKeyboardDidHideNotification, object: nil, queue: mainQueue, usingBlock: moveLoginBoxDown)
  }
  
  override func viewWillDisappear(animated: Bool) {
    super.viewWillDisappear(animated)
    (UIApplication.sharedApplication().delegate as! AppDelegate).startBackgroundSync()
    logger.debug("Un-registering observer for keyboard notification to free up resources and prevent memory leaks")
    let notificationCenter = NSNotificationCenter.defaultCenter()
    if let keyboardWillShowObserver = keyboardWillShowObserver {
      notificationCenter.removeObserver(keyboardWillShowObserver)
    }
    if let keyboardDidShowObserver = keyboardDidShowObserver {
      notificationCenter.removeObserver(keyboardDidShowObserver)
    }
    if let keyboardWillHideObserver = keyboardWillHideObserver {
      notificationCenter.removeObserver(keyboardWillHideObserver)
    }
    if let keyboardDidHideObserver = keyboardDidHideObserver {
      notificationCenter.removeObserver(keyboardDidHideObserver)
    }
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
