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
import _1PasswordExtension

class LoginViewController: UIViewController, UITextFieldDelegate, POPAnimationDelegate {

    private struct constantValues {
        static let loginBoxAnimationKey = "moveLoginBoxAnimation"
        static let wrongPasswordAnimationKey = "shakePassword"
    }
    
    @IBOutlet weak var loginBox: UIView! {
        didSet {
            initialLoginPosition = loginBox.center.y
        }
    }
    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var hostTextField: UITextField!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    private var initialLoginPosition: CGFloat?
    
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
                        if (error.code != AppExtensionErrorCodeCancelledByUser) {
                            println("Unable to load password for URLString \(urlString)")
                        } else {
                            println("Cancelled by user")
                        }
                    }
            }
        } else {
            // TODO: Show alert informing about password manager
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
                hostname = "https://" + hostname
            }
            if hostname.hasSuffix("/") {
                hostname.removeAtIndex(advance(hostname.startIndex, count(hostname) - 1))
            }
            
            Locksmith.saveData([Constants.keychainUsernameField: username, Constants.keychainPasswordField: password, Constants.keychainDomainField: hostname], forUserAccount: Constants.userAccount)
            
            validateCurrentLogin()
        } else {
            animateInvalidLogin()
        }
    }
    
    // This function uses the keychain to try and log into the system
    func validateCurrentLogin() {
        activityIndicator.startAnimating()
        
        let (dictionary, error) = Locksmith.loadDataForUserAccount(Constants.userAccount)
        if let currentError = error {
            println("An error occured while loading keychain data")
            activityIndicator.stopAnimating()
        } else if dictionary?.count == 0 {
            println("Keychain dictionary is empty")
            activityIndicator.stopAnimating()
        } else if let keychainDictionary = dictionary as? [String: String] {
            if let username = keychainDictionary[Constants.keychainUsernameField], password = keychainDictionary[Constants.keychainPasswordField], domain = keychainDictionary[Constants.keychainDomainField] {
     
                // Update UI
                usernameTextField.text = username
                passwordTextField.text = password
                hostTextField.text = domain
                
                // Create request
                let manager = AFHTTPRequestOperationManager()
                let parameters = ["username": username,
                    "password": password,
                    "rememberMe": "on"]
                
                manager.POST(domain + Constants.API.login,
                    parameters: parameters,
                    success:
                    {
                        requestOperation, response in
                            println("Successfully logged in")
                            if(requestOperation.response.statusCode == 200) {
                                println("Credentials valid")
                                dispatch_async(dispatch_get_main_queue()) {
                                    self.activityIndicator.stopAnimating()
                                }
                            } else {
                                println("Credentials invalid")
                                self.animateInvalidLogin()
                            }
                    },
                    failure:
                    {
                        requestOperation, error in
                            println("Unable to log in")
                            self.animateInvalidLogin()
                    }
                )
            } else {
                println("Unable to retrieve required fields")
                animateInvalidLogin()
            }
        } else {
            println("Unable to load keychain items")
            animateInvalidLogin()
        }
    }
    
    private func animateInvalidLogin() {
        // Create animation in case of log-in failure
        let shake = POPSpringAnimation(propertyNamed: kPOPLayerPositionX)
        shake.springBounciness = 20
        shake.velocity = 3000
        
        dispatch_async(dispatch_get_main_queue()) {
            self.loginBox.pop_addAnimation(shake, forKey: constantValues.wrongPasswordAnimationKey)
            self.activityIndicator.stopAnimating()
        }
        usernameTextField.becomeFirstResponder()
    }
    
    // MARK: - Text field animation and keyboard management
    
    /// This function is invoked, as soon as a touch outside the field was recognized. The keyboard is dismissed by calling resignFirstResponder on the currently edited textfield.
    func dismissKeyboard(gesture: UITapGestureRecognizer) {
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
        if let yPosition = initialLoginPosition {
            moveLoginBox(yPosition)
        }
    }
    
    /// This function is moving the login box to the parameter value using a pop spring animation
    private func moveLoginBox(toValue: CGFloat) {
        if loginBox.center.y != toValue {
            var moveAnimation = POPSpringAnimation(propertyNamed: kPOPLayerPositionY)
            moveAnimation.springBounciness = 15
            moveAnimation.springSpeed = 15
            moveAnimation.toValue = toValue
            moveAnimation.delegate = self
            moveAnimation.removedOnCompletion = true
            moveAnimation.name = constantValues.loginBoxAnimationKey
            loginBox.pop_addAnimation(moveAnimation, forKey: constantValues.loginBoxAnimationKey)
        }
    }
    
    // MARK: - ViewController Lifecycle
    
    /// The function registers gesture recognizer dismissing the keyboard
    override func viewDidLoad() {
        super.viewDidLoad()
        view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "dismissKeyboard:"))
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardWillShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardDidShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardDidHideNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardWillHideNotification, object: nil)
        validateCurrentLogin()
    }
}

