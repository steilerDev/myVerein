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
//  ToggleCell.swift
//  This file contains all necessary information for the custom cell containing a toggle, as well as a delegate when the toggle's state was changed.
//

import UIKit

// TODO: Make it IBDesignable one day with a bigger machine
class ToggleCell: UITableViewCell {

  var rootCell: UITableViewCell!
  
  @IBOutlet weak var label: UILabel!
  @IBOutlet weak var toggle: UISwitch!
  
  var delegate: ToggleCellDelegate?
  
  @IBInspectable var title: String? {
    get {
      return label.text
    }
    set {
      label.text = newValue
    }
  }
  
  /// Get or set the state of the cell
  @IBInspectable var enabled: Bool {
    get {
      return label.enabled && toggle.enabled
    }
    set {
      label.enabled = newValue
      toggle.enabled = newValue
    }
  }
  
  /// Get or set the state of the cell's toggle
  @IBInspectable var toggleState: Bool {
    get {
      return toggle.on
    }
    set {
      toggle.setOn(newValue, animated: true)
    }
  }
  
  override init(style: UITableViewCellStyle, reuseIdentifier: String?) {
    super.init(style: style, reuseIdentifier: reuseIdentifier)
    setup()
  }
  
  required init(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    setup()
  }
  
  private func setup() {
    rootCell = NSBundle.mainBundle().loadNibNamed("ToggleCell", owner: self, options: nil).first as! UITableViewCell
    
    rootCell.frame = self.bounds
    rootCell.autoresizingMask = .FlexibleWidth | .FlexibleHeight
    
    self.addSubview(rootCell)
  }
  
  override func awakeFromNib() {
    super.awakeFromNib()
    self.selectionStyle = .None
    toggle.addTarget(self, action: "toggleDidChangeState:", forControlEvents: .ValueChanged)
  }

  func toggleDidChangeState(sender: UISwitch) {
    delegate?.didChangeState(self)
  }
}

// MARK: - Toggle cell delegate protocol definition

protocol ToggleCellDelegate {
  func didChangeState(sender: ToggleCell)
}