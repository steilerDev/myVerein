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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/user/event")
public class EventController
{
    private static Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private EventRepository eventRepository;

    /**
     * This function gathers all events for the currently logged in user. If lastChanged is stated only events that changed after that moment are returned.
     * @param lastChanged The date of the last changed action, correctly formatted (YYYY-MM-DDTHH:mm:ss)
     * @param currentUser The currently logged in user
     * @return A list of all events for the user that changed since the last changed moment in time (only containing id's)
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getAllEventsForUser(@RequestParam(required = false) String lastChanged, @CurrentUser User currentUser)
    {
        List<Event> events;
        if(lastChanged != null && !lastChanged.isEmpty())
        {
            logger.debug("[" + currentUser + "] Gathering all user events changed after " + lastChanged);
            LocalDateTime lastChangedTime;
            try
            {
                lastChangedTime = LocalDateTime.parse(lastChanged, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch(DateTimeParseException e)
            {
                logger.warn("[" + currentUser + "] Unable to get all events for user, because the last changed format is wrong: " + e.getLocalizedMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            events = eventRepository.findAllByPrefixedInvitedUserAndLastChangedLessThan(Event.prefixedUserIDForUser(currentUser), lastChangedTime);
        } else
        {
            logger.debug("[" + currentUser + "] Gathering all user events");
            events = eventRepository.findAllByPrefixedInvitedUser(Event.prefixedUserIDForUser(currentUser));
        }

        if(events == null || events.isEmpty())
        {
            logger.warn("[" + currentUser + "] No events to return");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            logger.info("[" + currentUser + "] Returning " + events.size() + " events");
            events.replaceAll(Event::getSendingObjectOnlyId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        }
    }

    @RequestMapping(produces = "application/json", params = "id", method = RequestMethod.GET)
    public ResponseEntity<Event> getEvent(@RequestParam String id, @CurrentUser User currentUser)
    {
        logger.debug("[" + currentUser + "] Gathering event with id " + id);
        if(id.isEmpty())
        {
            logger.warn("[" + currentUser + "] The id is not allowed to be empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            Event event = eventRepository.findEventById(id);
            if (event == null)
            {
                logger.warn("[" + currentUser + "] Unable to find event with id " + id);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } else
            {
                logger.info("[" + currentUser + "] Returning event " + event);
                return new ResponseEntity<>(event.getSendingObjectInternalSync(), HttpStatus.OK);
            }
        }
    }
}
