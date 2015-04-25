/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package de.steilerdev.myVerein.server.controller.user;

import de.steilerdev.myVerein.server.model.Event;
import de.steilerdev.myVerein.server.model.Event.EventStatus;
import de.steilerdev.myVerein.server.model.EventRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class handles gathering information about events by a user.
 */
@RestController
@RequestMapping("/api/user/event")
public class EventController
{
    private static Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventRepository eventRepository;

    /**
     * This function gathers all events for the currently logged in user. If lastChanged is stated only events that changed after that moment are returned. The function is invoked by GETting the URI /api/user/event.
     * @param lastChanged The date of the last changed action, correctly formatted (YYYY-MM-DDTHH:mm:ss)
     * @param currentUser The currently logged in user
     * @return A response entity containing a list of all events for the user that changed since the last changed moment in time (only containing their ids), or all events if the parameter was not specified. In case of an error, the response entity only contains the error code.
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getAllEventsForUser(@RequestParam(required = false) String lastChanged, @CurrentUser User currentUser)
    {
        List<Event> events;
        // Gathering events
        if (lastChanged != null && !lastChanged.isEmpty())
        {
            logger.debug("[{}] Gathering all user events changed after {}", currentUser, lastChanged);
            LocalDateTime lastChangedTime;
            try
            {
                lastChangedTime = LocalDateTime.parse(lastChanged, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e)
            {
                logger.warn("[{}] Unable to get all events for user, because the last changed format is wrong: {}", currentUser, e.getLocalizedMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            events = eventRepository.findAllByPrefixedInvitedUserAndLastChangedAfter(Event.prefixedUserIDForUser(currentUser), lastChangedTime);
        } else
        {
            logger.debug("[{}] Gathering all user events", currentUser);
            events = eventRepository.findAllByPrefixedInvitedUser(Event.prefixedUserIDForUser(currentUser));
        }

        // Checking and returning events
        if (events == null || events.isEmpty())
        {
            logger.warn("[{}] No events to return", currentUser);
            return new ResponseEntity<>(HttpStatus.OK);
        } else
        {
            logger.info("[{}] Returning {} events", currentUser, events.size());
            events.replaceAll(Event::getSendingObjectOnlyId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        }
    }

    /**
     * This function retrieves the event specified by it's id and returns all appropriate information about the event. The function is invoked by GETting the URI /api/user/event using the parameter id.
     * @param eventID The id of the searched event.
     * @param currentUser The currently logged in user.
     * @return A response entity containing the specified event, reduced to it's appropriate information. In case of an error, the response entity only contains an error code.
     */
    @RequestMapping(produces = "application/json", params = "id", method = RequestMethod.GET)
    public ResponseEntity<Event> getEvent(@RequestParam(value = "id") String eventID, @CurrentUser User currentUser)
    {
        Event event;
        logger.trace("[{}] Gathering event with id {}", currentUser, eventID);
        if (eventID.isEmpty())
        {
            logger.warn("[{}] The id is not allowed to be empty", currentUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if ((event = eventRepository.findEventById(eventID)) == null)
        {
            logger.warn("[{}] Unable to find event with id {}", currentUser, eventID);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            logger.info("[{}] Returning event {}", currentUser, event);
            return new ResponseEntity<>(event.getSendingObjectInternalSync(currentUser), HttpStatus.OK);
        }
    }

    /**
     * This function accepts a user response to a specific event and stores this response. The function is invoked by POSTing the parameter id to the URI /api/user/event.
     * @param responseString The string representation of the enumeration {@link de.steilerdev.myVerein.server.model.Event.EventStatus EventStatus}.
     * @param eventID The event id of the event the user is responding to.
     * @param currentUser The currently logged in user.
     * @return A response entity containing a success code, in case of a successful execution or an error code in case of a failure.
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.POST)
    public ResponseEntity respondToEvent(@RequestParam(value = "response") String responseString, @RequestParam(value = "id") String eventID, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Responding to event {} with {}", currentUser, eventID, responseString);
        Event event;
        if(eventID.isEmpty() || responseString.isEmpty())
        {
            logger.warn("[{}] The event or response is not allowed to be empty", currentUser);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else if((event = eventRepository.findEventById(eventID)) == null)
        {
            logger.warn("[{}] Unable to gather the specified event with id {}", currentUser, eventID);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else if(!event.getInvitedUser().containsKey(currentUser.getId()))
        {
            logger.warn("[{}] User is not invited to the event", currentUser);
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        } else
        {
            EventStatus response;
            try
            {
                response = EventStatus.valueOf(responseString.toUpperCase());
                if(response == EventStatus.REMOVED || response == EventStatus.PENDING)
                {
                    logger.warn("[{}] Unable to select 'removed' or 'pending' as a response for an event");
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
            } catch (IllegalArgumentException e)
            {
                logger.warn("[{}] Unable to parse response: {}", currentUser, e.getMessage());
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }

            event.getInvitedUser().put(currentUser.getId(), response);
            eventRepository.save(event);
            logger.info("[{}] Successfully responded to event {} with response {}", currentUser, event, response);
            return new ResponseEntity(HttpStatus.OK);
        }
    }

    /**
     * This function gathers and returns the user, that chose the specified response for the specified event. The function is invoked by GETting the URI /api/user/event with the response and event id.
     * @param responseString The string representation of the enumeration {@link de.steilerdev.myVerein.server.model.Event.EventStatus EventStatus}, used to specify the searched group of user.
     * @param eventID The event id of the searched event.
     * @param currentUser The currently logged in user.
     * @return A response entity containing a list of user ids and a success code, in case of a successful execution, or an error code in case of a failure.
     */
    @RequestMapping(produces = "application/json", params = {"id", "response"}, method = RequestMethod.GET)
    public ResponseEntity<List<String>> getResponsesOfEvent(@RequestParam(value = "response") String responseString, @RequestParam(value = "id") String eventID, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Finding all responses of type {} to event {}", currentUser, responseString, eventID);
        Event event;
        if(eventID.isEmpty())
        {
            logger.warn("[{}] The event id is not allowed to be empty", currentUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if((event = eventRepository.findEventById(eventID)) == null)
        {
            logger.warn("[{}] Unable to gather the specified event with id {}", currentUser, eventID);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(!event.getInvitedUser().containsKey(currentUser.getId()))
        {
            logger.warn("[{}] User is not invited to the event and therefore not allowed to view this list", currentUser);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else
        {
            EventStatus response;
            try
            {
                response = EventStatus.valueOf(responseString.toUpperCase());
            } catch (IllegalArgumentException e)
            {
                logger.warn("[{}] Unable to parse response: {}", currentUser, e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Filtering invited user matching the response
            HashMap<String, EventStatus> invitedUser = new HashMap<>();
            List<String> matchingUser = invitedUser.keySet().stream().filter(userID -> invitedUser.get(userID) == response).collect(Collectors.toList());

            logger.info("[{}] Successfully gathered all user matching the response {} for event {}", currentUser, response, event);
            return new ResponseEntity<>(matchingUser, HttpStatus.OK);
        }
    }
}
