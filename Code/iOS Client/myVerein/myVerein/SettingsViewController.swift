//
//  SettingsViewController.swift
//  myVerein
//
//  Created by Frank Steiler on 28/03/15.
//  Copyright (c) 2015 steilerDev. All rights reserved.
//

import UIKit

class SettingsViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
  @IBAction func logoutPress() {
    MVSecurity.instance().updateKeychain(nil, newPassword: nil, newDomain: nil)
    (UIApplication.sharedApplication().delegate as! AppDelegate).showLoginView()
  }

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
