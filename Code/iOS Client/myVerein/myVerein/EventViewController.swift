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
//  EventViewController.swift
//  This file holds all information related to the event view, including event loading and delegate methods for click events.
//

import UIKit
import XCGLogger

class EventViewController: UITableViewController {
  let logger = XCGLogger.defaultInstance()
}

// MARK: - Delegate methods for click events
extension EventViewController {  
  @IBAction func doneButonPressed(sender: UIBarButtonItem) {
    dismissViewControllerAnimated(true, completion: {})
  }
}

// MARK: - UIViewController lifecycle methods
extension EventViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    // Uncomment the following line to preserve selection between presentations
    // self.clearsSelectionOnViewWillAppear = false
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem()
  }
  
  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
    // Dispose of any resources that can be recreated.
  }
  
  /*
  // MARK: Navigation
  
  // In a storyboard-based application, you will often want to do a little preparation before navigation
  override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
  // Get the new view controller using [segue destinationViewController].
  // Pass the selected object to the new view controller.
  }
  */
}

// MARK: - Table view data source
extension EventViewController {
  
//  override func numberOfSectionsInTableView(tableView: UITableView) -> Int {
//    // #warning Potentially incomplete method implementation.
//    // Return the number of sections.
//    return 0
//  }
//  
//  override func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
//    // #warning Incomplete method implementation.
//    // Return the number of rows in the section.
//    return 0
//  }
  
  /*
  override func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
  let cell = tableView.dequeueReusableCellWithIdentifier("reuseIdentifier", forIndexPath: indexPath) as! UITableViewCell
  
  // Configure the cell...
  
  return cell
  }
  */
}
