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

import CoreData

class LoginViewController: UIViewController, UITextFieldDelegate, POPAnimationDelegate {

    /// Struct containing String constants only used by this class
    private struct LoginViewControllerConstants {
        static let LoginBoxAnimationKey = "moveLoginBoxAnimation"
        static let WrongPasswordAnimationKey = "shakePassword"
        static let SegueToMainApplication = "showMainApplicationSegue"
        
        static let PasswordManagerURL = "https://agilebits.com/onepassword"
    }
    
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

    
    // MARK: - UITextFieldDelegate methods
    
    func textFieldShouldReturn(textField: UITextField) -> Bool {
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
    
    // MARK: - Password management extension
    
    @IBAction func openPasswordManager(sender: UIButton) {
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
                    if let login = loginDict as? [String: String] {
                        if let username = login[AppExtensionUsernameKey], password = login[AppExtensionPasswordKey], domain = login[AppExtensionURLStringKey] {
                            self.usernameTextField.text = username
                            self.passwordTextField.text = password
                            self.hostTextField.text = domain
                        } else {
                            println("Unable to get required fields")
                        }
                    } else {
                        if (error.code != Int(AppExtensionErrorCodeCancelledByUser)) {
                            println("Unable to load password for URLString \(urlString)")
                        } else {
                            println("Cancelled by user")
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
            
            println("Unable to find any password manager")
        }
    }
    
    // MARK: - Log in
    
    /// This function stores the current text fields within the keychain and then validates them using the validate login method
    @IBAction func loginAction() {
        if var hostname = hostTextField.text, username = usernameTextField.text, password = passwordTextField.text {
            if hostname.isEmpty || username.isEmpty || password.isEmpty {
                println("A required field is empty")
                animateInvalidLogin()
                return
            }
            //Adjusting hostname to meet required format
            if !hostname.hasPrefix("http") {
                hostname = "http://" + hostname
            }
            if hostname.hasSuffix("/") {
                hostname.removeAtIndex(advance(hostname.startIndex, count(hostname) - 1))
            }
            
            MVSecurity.instance().updateKeychain(username, newPassword: password, newDomain: hostname)            
            validateCurrentLogin()
        } else {
            animateInvalidLogin()
        }
    }
    
    // This function uses the keychain to try and log into the system
    func validateCurrentLogin() {
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
                    
            NetworkingAction.loginAction(
                {
                    self.performSegueWithIdentifier(LoginViewControllerConstants.SegueToMainApplication, sender: self)
                    self.activityIndicator.stopAnimating()
                },
                failure:
                {
                    error in
                    self.animateInvalidLogin()
                }
            )
        } else {
            println(MVError.createError(MVErrorCodes.MVKeychainEmptyError))
            animateInvalidLogin()
            
            usernameTextField.text = nil
            passwordTextField.text = nil
            hostTextField.text = nil
        }
    }
    
    private func animateInvalidLogin() {
        usernameTextField.enabled = true
        passwordTextField.enabled = true
        hostTextField.enabled = true
        loginButton.enabled = true
        
        // Create animation in case of log-in failure
        let shake = POPSpringAnimation(propertyNamed: kPOPLayerPositionX)
        shake.springBounciness = 20
        shake.velocity = 3000
        
        
        if(usernameTextField.isFirstResponder() || passwordTextField.isFirstResponder() || hostTextField.isFirstResponder()) {
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
        moveLoginBox(computedLoginPosition)
    }
    
    /// This function is invoked when the notification center fires the keyboard did hide notification
    func moveLoginBoxDown() {
        moveLoginBox(nil)
    }
    
    /// This function is moving the login box to the parameter value using a pop spring animation
    private func moveLoginBox(toValue: CGFloat?) {
        view.removeConstraint(topConstraintForLoginBox)
        
        var newCenter = CGPoint()
        newCenter.x = view.center.x
        
        newCenter.y = toValue ?? initialLoginPosition?.y ?? (loginBox.frame.size.height/2) + 24 //Use either the to value or the initial value, default should never be used
        
        var moveAnimation = POPSpringAnimation(propertyNamed: kPOPViewCenter)
        moveAnimation.springBounciness = 10
        moveAnimation.springSpeed = 10
        moveAnimation.toValue = NSValue(CGPoint: newCenter)
        moveAnimation.delegate = self
        moveAnimation.name = LoginViewControllerConstants.LoginBoxAnimationKey
        loginBox.pop_addAnimation(moveAnimation, forKey: LoginViewControllerConstants.LoginBoxAnimationKey)
    }
    
    func pop_animationDidReachToValue(anim: POPAnimation!) {
        view.removeConstraint(newVariableTopConstraint)
        newVariableTopConstraint.constant = loginBox.center.y
        view.addConstraint(newVariableTopConstraint)
    }
    
    // MARK: - ViewController Lifecycle
    
    /// The function registers gesture recognizer dismissing the keyboard
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardWillShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardDidShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardDidHideNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardWillHideNotification, object: nil)
        initialLoginPosition = loginBox.center
        validateCurrentLogin()
    }
}

