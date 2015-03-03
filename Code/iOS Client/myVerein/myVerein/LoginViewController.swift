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

class LoginViewController: UIViewController, UITextFieldDelegate, POPAnimationDelegate {

    private struct LoginViewControllerConstantValues {
        static let loginBoxAnimationKey = "moveLoginBoxAnimation"
        static let wrongPasswordAnimationKey = "shakePassword"
        static let segueToMainApplication = "showMainApplicationSegue"
    }
    
    @IBOutlet weak var loginBox: UIView!
    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var hostTextField: UITextField!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    @IBOutlet weak var topConstraintForLoginBox: NSLayoutConstraint!
    
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
                UIApplication.sharedApplication().openURL(NSURL(string: Constants.passwordManagerURL)!)
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
            
            if Locksmith.loadDataForUserAccount(Constants.userAccount).0?.count > 0 {
                Locksmith.updateData([Constants.keychainUsernameField: username, Constants.keychainPasswordField: password, Constants.keychainDomainField: hostname], forUserAccount: Constants.userAccount)
            } else {
                Locksmith.saveData([Constants.keychainUsernameField: username, Constants.keychainPasswordField: password, Constants.keychainDomainField: hostname], forUserAccount: Constants.userAccount)
            }
            
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
            if let username = keychainDictionary[Constants.keychainUsernameField],
                password = keychainDictionary[Constants.keychainPasswordField],
                domain = keychainDictionary[Constants.keychainDomainField]
            {
                // Update UI
                usernameTextField.text = username
                passwordTextField.text = password
                hostTextField.text = domain
                
                if let sessionManager = NetworkingSessionFactory.instance() {
                    println(sessionManager.securityPolicy.pinnedCertificates.count)
                    
                    // Execute request
                    let parameters = ["username": username,
                        "password": password,
                        "rememberMe": "on"]
                    
                    sessionManager.POST(NSURL(string: Constants.API.login, relativeToURL: sessionManager.baseURL)?.absoluteString,
                        parameters: parameters,
                        success:
                        {
                            dataTask, response in
                            println("Successfully logged in")
                            dispatch_async(dispatch_get_main_queue()) {
                                self.activityIndicator.stopAnimating()
                            }
                            //self.performSegueWithIdentifier(LoginViewControllerConstantValues.segueToMainApplication, sender: self)
                        },
                        failure:
                        {
                            dataTask, error in
                            println("Unable to log in: \(error)")
                            self.animateInvalidLogin()
                            NetworkingSessionFactory.invalidateInstance()
                        }
                    )
                } else {
                    println("Nope")
                    animateInvalidLogin()
                }
                
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
        
        
        if(usernameTextField.isFirstResponder() || passwordTextField.isFirstResponder() || hostTextField.isFirstResponder()) {
            dispatch_async(dispatch_get_main_queue()) {
                self.loginBox.pop_addAnimation(shake, forKey: LoginViewControllerConstantValues.wrongPasswordAnimationKey)
            }
        } else {
            usernameTextField.becomeFirstResponder()
        }
        dispatch_async(dispatch_get_main_queue()) { self.activityIndicator.stopAnimating() }
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
        moveLoginBox(nil)
    }
    
    /// This function is moving the login box to the parameter value using a pop spring animation
    private func moveLoginBox(toValue: CGFloat?) {
        view.removeConstraint(topConstraintForLoginBox)
        
        var newCenter = CGPoint()
        newCenter.x = view.center.x
        
        newCenter.y = toValue ?? initialLoginPosition?.y ?? (loginBox.frame.size.height/2) + 24 //Use either the to value or the initial value, default should never be used
        
        var moveAnimation = POPSpringAnimation(propertyNamed: kPOPViewCenter)
        moveAnimation.springBounciness = 15
        moveAnimation.springSpeed = 15
        moveAnimation.toValue = NSValue(CGPoint: newCenter)
        moveAnimation.delegate = self
        moveAnimation.name = LoginViewControllerConstantValues.loginBoxAnimationKey
        loginBox.pop_addAnimation(moveAnimation, forKey: LoginViewControllerConstantValues.loginBoxAnimationKey)
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
        view.addGestureRecognizer(UITapGestureRecognizer(target: self, action: "dismissKeyboard:"))
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardWillShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxUp:", name: UIKeyboardDidShowNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardDidHideNotification, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "moveLoginBoxDown", name: UIKeyboardWillHideNotification, object: nil)
        initialLoginPosition = loginBox.center
        validateCurrentLogin()
    }
}

