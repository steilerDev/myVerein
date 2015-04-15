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

// TODO: Add description field

import UIKit
import XCGLogger
import MapKit

class EventViewController: UITableViewController {
  let logger = XCGLogger.defaultInstance()
  
  @IBOutlet weak var titleBar: UINavigationItem!
  @IBOutlet weak var eventTitle: UILabel!
  @IBOutlet weak var eventLocation: UILabel!
  @IBOutlet weak var eventTimes: UILabel!
  
  @IBOutlet weak var mapView: MKMapView!
  
  @IBOutlet weak var goingCell: UITableViewCell!
  @IBOutlet weak var maybeCell: UITableViewCell!
  @IBOutlet weak var declineCell: UITableViewCell!
  @IBOutlet weak var participantCell: UITableViewCell!
  
  var event: Event?
}

// MARK: - UIViewController lifecycle methods
extension EventViewController {
  override func viewDidLoad() {
    super.viewDidLoad()
    
    // Hiding title on back button for ParticipantViewController (See http://justabeech.com/2014/02/24/empty-back-button-on-ios7/ for reference)
    let backButton = UIBarButtonItem(title: "", style: .Plain, target: nil, action: nil)
    navigationItem.backBarButtonItem = backButton
    
    // Populating view using event
    if let event = event {
      logger.debug("Succesfully loaded event, populating view")
      titleBar.title = event.title
      eventTitle.text = event.title
      
      eventTimes.text = event.dateStringLong
      eventLocation.text = event.locationString
      
      if let response = event.response {
        switch response {
        case .Going:
          goingCell.accessoryType = .Checkmark
        case .Maybe:
          maybeCell.accessoryType = .Checkmark
        case .Decline:
          declineCell.accessoryType = .Checkmark
        default: break;
        }
      }
      if event.locationLat != nil && event.locationLng != nil {
        mapView.addAnnotation(event)
        mapView.showAnnotations([event], animated: false)
        mapView.selectAnnotation(event, animated: false)
      }
    } else {
      logger.error("Unable to load event for event detail view, dismissing view controller")
      navigationController?.popViewControllerAnimated(true)
    }
  }
  
  override func viewDidDisappear(animated: Bool) {
    super.viewDidDisappear(animated)
    logger.debug("View did disappear, sending response for event")
    if let event = event {
      MVNetworkingHelper.sendEventResponse(event)
    } else {
      logger.warning("Not sending any event response, because event is nil")
    }
  }
  
  // MARK: Navigation
  override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
    if let identifier = segue.identifier {
      switch identifier {
      case EventViewControllerConstants.SegueToParticipants:
        logger.debug("Preparing segue to participant list")
        if let destinationViewController = segue.destinationViewController as? ParticipantViewController {
          destinationViewController.event = event
        } else {
          logger.error("Unable to get destination view controller")
        }
      default: break;
      }
    } else {
      logger.error("Unable to get segue identifier")
    }
  }
}

// MARK: - UITableView delegate methods
extension EventViewController {
  
  /// This function handles the click on the participants row or the click on an answer about the user's participation
  override func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
    logger.debug("Did select row at index path: \(indexPath)")
    if indexPath.length == 2 {
      if indexPath.indexAtPosition(0) == 0 && indexPath.indexAtPosition(1) == 1 {
        logger.info("Selected participants cell, performing segue for event \(self.event)")
        performSegueWithIdentifier(EventViewControllerConstants.SegueToParticipants, sender: nil)
        participantCell.selected = false
      } else if indexPath.indexAtPosition(0) == 2 {
        maybeCell.accessoryType = .None
        goingCell.accessoryType = .None
        declineCell.accessoryType = .None
        switch indexPath.indexAtPosition(1) {
        case 0:
          logger.info("Selected going response cell")
          goingCell.accessoryType = .Checkmark
          event?.response = .Going
          goingCell.selected = false
        case 1:
          logger.info("Selected maybe response cell")
          maybeCell.accessoryType = .Checkmark
          event?.response = EventResponse.Maybe
          maybeCell.selected = false
        case 2:
          logger.info("Selected decline response cell")
          declineCell.accessoryType = .Checkmark
          event?.response = .Decline
          declineCell.selected = false
        default:
          logger.warning("Selected unintended cell!")
          event?.response = .Pending
        }
      } else {
        logger.warning("Selected unintended cell!")
      }
    } else {
      logger.warning("Length of index path does not fit \(indexPath.length)")
    }
  }
}

// MARK: - MapView delegate methods
extension EventViewController: MKMapViewDelegate {
  func mapView(mapView: MKMapView!, viewForAnnotation annotation: MKAnnotation!) -> MKAnnotationView! {
    var view = mapView.dequeueReusableAnnotationViewWithIdentifier(EventViewControllerConstants.ReuseAnnotationIdentifier)
    
    if view == nil {
      view = MKPinAnnotationView(annotation: annotation, reuseIdentifier: EventViewControllerConstants.ReuseAnnotationIdentifier)
      view.canShowCallout = true
      view.rightCalloutAccessoryView = UIButton.buttonWithType(.DetailDisclosure) as! UIButton
    } else {
      view.annotation = annotation
    }
    return view
  }
  
  func mapView(mapView: MKMapView!, annotationView view: MKAnnotationView!, calloutAccessoryControlTapped control: UIControl!) {
    if let event = event {
      logger.info("Opening event in maps app")
      let placemark = MKPlacemark(coordinate: event.coordinate, addressDictionary: nil)
      let mapItem = MKMapItem(placemark: placemark)
      mapItem.name = event.title
      mapItem.openInMapsWithLaunchOptions(nil)
    }
  }
}
// MARK: - EventViewController related constants
struct EventViewControllerConstants {
  static let ReuseAnnotationIdentifier = "annotation"
  static let SegueToParticipants = "showParticipants"
}
