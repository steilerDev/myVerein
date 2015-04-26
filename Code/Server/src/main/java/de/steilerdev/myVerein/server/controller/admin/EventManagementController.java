/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.steilerdev.myVerein.server.controller.admin;

import de.steilerdev.myVerein.server.model.*;
import de.steilerdev.myVerein.server.model.division.Division;
import de.steilerdev.myVerein.server.model.division.DivisionRepository;
import de.steilerdev.myVerein.server.model.event.Event;
import de.steilerdev.myVerein.server.model.event.EventRepository;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This controller is processing all requests associated with the event management.
 */
@RestController
@RequestMapping("/api/admin/event")
public class EventManagementController
{
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    private final Logger logger = LoggerFactory.getLogger(EventManagementController.class);

    /**
     * These final strings are used to bundle information within a response entity.
     */
    final String successMessage = "successMessage", errorMessage = "errorMessage", eventId = "eventId";

    /**
     * This function gathers all dates where an event takes place within a specific month and year. The function is invoked by GETting the URI /api/admin/event and specifying the month and year via the request parameters.
     * @param month The selected month.
     * @param year The selected year.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If the function succeeds, a list of dates, during the month, that contain an event, is returned, otherwise an error code is returned.
     */
    @RequestMapping(params = {"month", "year"}, produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<LocalDate>> getEventDatesOfMonth(@RequestParam String month,
                                                                @RequestParam String year,
                                                                @CurrentUser User currentUser)
    {
        logger.trace("[{}] Gathering events of month {} and year {}", currentUser, month, year);

        int monthInt, yearInt;
        try
        {
            monthInt = Integer.parseInt(month);
            yearInt = Integer.parseInt(year);
        } catch (NumberFormatException e)
        {
            logger.warn("[{}] Unable to parse month ({}) or year ({})", currentUser, month, year);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        LocalDateTime start = LocalDate.of(yearInt, monthInt, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);

        List<Event> eventsOfMonth = eventRepository.findAllSpanningOverPeriod(start, end);

        if(eventsOfMonth == null)
        {
            logger.warn("[{}] Unable to gather events of month {} in year {}", currentUser, monthInt, yearInt);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            logger.debug("[{}] Extracting occupied dates for month {} in year {}", currentUser, monthInt, yearInt);
            ArrayList<LocalDate> dates = new ArrayList<>();
            eventsOfMonth.parallelStream()
                    .forEach(event -> {//Creating a local date for each occupied date of the event
                        for (LocalDate date = event.getStartDateTime().toLocalDate(); //Starting with the start date
                             !date.equals(event.getEndDateTime().toLocalDate()); //Until reaching the end date
                             date = date.plusDays(1)) //Adding a day within each iterations
                        {
                            dates.add(date);
                        }
                        dates.add(event.getEndDateTime().toLocalDate()); //Finally adding the last date
                    });
            List<LocalDate> distinctDates = dates.stream().distinct().collect(Collectors.toList());
            logger.debug("[{}] Returning {} distinct occupied dates for month {} in year {}", currentUser, distinctDates.size(), monthInt, yearInt);
            return new ResponseEntity<>(distinctDates, HttpStatus.OK);
        }
    }

    /**
     * Returns all events, that are taking place on a specified date. The date parameter needs to be formatted according to the following pattern: YYYY/MM/DD. This function is invoked by GETting the URI /api/admin/event with the parameter date.
     * @param date The selected date, correctly formatted (YYYY/MM/DD)
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If the function succeeds, a list of events is returned, otherwise an error code is returned.
     */
    @RequestMapping(params = "date", produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getEventsOfDate(@RequestParam String date, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Getting events of date {}", currentUser, date);
        LocalDateTime startOfDay, endOfDay;
        List<Event> eventsOfDay;
        try
        {
            // Get start of day and start of next day
            startOfDay = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            endOfDay = startOfDay.plusDays(1);

            logger.debug("[{}] Converted to date object {}", currentUser, startOfDay);
        } catch (DateTimeParseException e)
        {
            logger.warn("[{}] Unable to parse date {}: {}", currentUser, date, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if((eventsOfDay = eventRepository.findAllSpanningOverPeriod(startOfDay, endOfDay)) == null)
        {
            logger.warn("[{}] Unable to get events spanning from {} to {}", currentUser, startOfDay, endOfDay);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            eventsOfDay.replaceAll(Event::getSendingObjectOnlyNameTimeId);
            logger.debug("[{}] Returning {} events for {}", currentUser, eventsOfDay.size(), date);
            return new ResponseEntity<>(eventsOfDay, HttpStatus.OK);
        }
    }

    /**
     * Returns a specific event based on its id. The function is invoked by GETting the URI the URI /api/admin/event.
     * @param eventId The id of the selected event.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If the function succeeds, the event is returned, otherwise an error code is returned.
     */
    @RequestMapping(params = "id", produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<Event> getEvent(@RequestParam(value = "id") String eventId, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Getting the event for ID {}", currentUser, eventId);
        Event selectedEvent;
        if(eventId.isEmpty())
        {
            logger.warn("[{}] The ID can not be empty", currentUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if((selectedEvent = eventRepository.findById(eventId)) == null)
        {
            logger.warn("[{}] Unable to find specified event");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            selectedEvent = selectedEvent.getSendingObjectWithDivisionNamesForWeb();
            if (!currentUser.isAllowedToAdministrate(selectedEvent))
            {
                logger.debug("[{}] The user is not allowed to edit the event {}", currentUser, selectedEvent);
                selectedEvent.setAdministrationNotAllowedMessage("You are not allowed to modify this event, since you did not create it");
            }
            logger.debug("[{}] Returning event {}", currentUser, selectedEvent);
            return new ResponseEntity<>(selectedEvent, HttpStatus.OK);
        }
    }

    /**
     * This function saves an event. The function is invoked by POSTint the parameters to the URI /api/admin/event.
     * @param eventFlag This flag either stores the ID of the event, or true, if a new event is created.
     * @param eventName The name of the event.
     * @param eventDescription The description of the event.
     * @param startDate The start date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param startTime The start time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param endDate The end date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param endTime The end time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param location The name of the location of the event.
     * @param locationLat The latitude of the location of the event.
     * @param locationLng The longitude of the location of the event.
     * @param invitedDivisions A comma separated list of invited divisions.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code together with a JSON map object, containing an 'errorMessage', or a 'successMessage' respectively. If the operation was successful the id of the event is accessible via 'eventId'.
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public  ResponseEntity<Map<String, String>> saveEvent(@RequestParam String eventFlag,
                                                          @RequestParam String eventName,
                                                          @RequestParam String eventDescription,
                                                          @RequestParam String startDate,
                                                          @RequestParam String startTime,
                                                          @RequestParam String endDate,
                                                          @RequestParam String endTime,
                                                          @RequestParam String location,
                                                          @RequestParam String locationLat,
                                                          @RequestParam String locationLng,
                                                          @RequestParam String invitedDivisions,
                                                          @CurrentUser User currentUser)
    {
        logger.trace("[{}] Saving event", currentUser);
        Map<String, String> responseMap = new HashMap<>();
        if(eventFlag.isEmpty())
        {
            logger.warn("[{}] The event flag is empty", currentUser);
            responseMap.put(errorMessage, "The event flag is not allowed to be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(eventFlag.equals("true"))
        {
            return createEvent(eventName, eventDescription, startDate, startTime, endDate, endTime, location, locationLat, locationLng, invitedDivisions, currentUser);
        } else
        {
            return modifyEvent(eventFlag, eventName, eventDescription, startDate, startTime, endDate, endTime, location, locationLat, locationLng, invitedDivisions, currentUser);
        }
    }

    /**
     * This function creates a new event using the provided parameter
     * @param eventName The name of the event.
     * @param eventDescription The description of the event.
     * @param startDate The start date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param startTime The start time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param endDate The end date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param endTime The end time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param location The name of the location of the event.
     * @param locationLat The latitude of the location of the event.
     * @param locationLng The longitude of the location of the event.
     * @param invitedDivisions A comma separated list of invited divisions.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code together with a JSON map object, containing an 'errorMessage', or a 'successMessage' respectively. If the operation was successful the id of the event is accessible via 'eventId'.
     */
    private ResponseEntity<Map<String, String>> createEvent(   String eventName,
                                                               String eventDescription,
                                                               String startDate,
                                                               String startTime,
                                                               String endDate,
                                                               String endTime,
                                                               String location,
                                                               String locationLat,
                                                               String locationLng,
                                                               String invitedDivisions,
                                                               User currentUser)
    {
        logger.debug("[{}] A new event is created", currentUser);
        return populateEvent(new Event(), eventName, eventDescription, startDate, startTime, endDate, endTime, location, locationLat, locationLng, invitedDivisions, currentUser);
    }

    /**
     * This function creates a new event using the provided parameter
     * @param eventId The id of the event that should be modified.
     * @param eventName The name of the event.
     * @param eventDescription The description of the event.
     * @param startDate The start date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param startTime The start time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param endDate The end date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param endTime The end time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param location The name of the location of the event.
     * @param locationLat The latitude of the location of the event.
     * @param locationLng The longitude of the location of the event.
     * @param invitedDivisions A comma separated list of invited divisions.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code together with a JSON map object, containing an 'errorMessage', or a 'successMessage' respectively. If the operation was successful the id of the event is accessible via 'eventId'.
     */
    private ResponseEntity<Map<String, String>> modifyEvent(String eventId,
                                                            String eventName,
                                                            String eventDescription,
                                                            String startDate,
                                                            String startTime,
                                                            String endDate,
                                                            String endTime,
                                                            String location,
                                                            String locationLat,
                                                            String locationLng,
                                                            String invitedDivisions,
                                                            User currentUser)
    {
        logger.debug("[{}] The event with id {} is modified", currentUser, eventId);
        Event event = eventRepository.findById(eventId);
        Map<String, String> responseMap = new HashMap<>();
        if(event == null)
        {
            logger.warn("[{}] Unable to find the specified event with id {}", currentUser, eventId);
            responseMap.put(errorMessage, "Unable to find the specified event");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(!currentUser.isAllowedToAdministrate(event))
        {
            logger.warn("[{}] The user is not allowed to alter the selected event {}", currentUser, event);
            responseMap.put(errorMessage, "You are not allowed to edit the selected event");
            return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
        } else
        {
            return populateEvent(event, eventName, eventDescription, startDate, startTime, endDate, endTime, location, locationLat, locationLng, invitedDivisions, currentUser);
        }
    }

    /**
     * This populates a given event using the provided parameter
     * @param event The event that should be populated.
     * @param eventName The name of the event.
     * @param eventDescription The description of the event.
     * @param startDate The start date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param startTime The start time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param endDate The end date, formatted according to the pattern d/MM/y, defined within the Java 8 DateTimeFormatter.
     * @param endTime The end time, formatted according to the pattern H:m, defined within the Java 8 DateTimeFormatter.
     * @param location The name of the location of the event.
     * @param locationLat The latitude of the location of the event.
     * @param locationLng The longitude of the location of the event.
     * @param invitedDivisions A comma separated list of invited divisions.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code together with a JSON map object, containing an 'errorMessage', or a 'successMessage' respectively. If the operation was successful the id of the event is accessible via 'eventId'.
     */
    private ResponseEntity<Map<String, String>> populateEvent(Event event,
                                                              String eventName,
                                                              String eventDescription,
                                                              String startDate,
                                                              String startTime,
                                                              String endDate,
                                                              String endTime,
                                                              String location,
                                                              String locationLat,
                                                              String locationLng,
                                                              String invitedDivisions,
                                                              User currentUser)
    {
        logger.debug("[{}] Populating event {}", currentUser, event);

        Map<String, String> responseMap = new HashMap<>();

        /*
            Parsing mandatory properties
         */

        if(eventName.isEmpty())
        {
            logger.warn("[{}] The event name for event {} is not allowed to be empty", currentUser, event);
            responseMap.put(errorMessage, "The event name is not allowed to be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty())
        {
            logger.warn("[{}] The date and times defining the event {} are not allowed to be empty", currentUser, event);
            responseMap.put(errorMessage, "The date and times defining the event are not allowed to be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(invitedDivisions.isEmpty())
        {
            logger.warn("[{}] The invited divisions for event {} are not allowed to be empty", currentUser, event);
            responseMap.put(errorMessage, "The invited divisions are not allowed to be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else
        {
            // Parsing name
            event.setName(eventName);

            // Parsing times
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/y'T'H:m");
            try
            {
                event.setStartDateTime(LocalDateTime.parse(startDate + "T" + startTime, formatter));
            } catch (DateTimeParseException e)
            {
                logger.warn("[{}] Unrecognized date format {}T{}", currentUser, startDate, startTime);
                responseMap.put(errorMessage, "Unrecognized date or time format within start time");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }

            try
            {
                event.setEndDateTime(LocalDateTime.parse(endDate + "T" + endTime, formatter));
            } catch (DateTimeParseException e)
            {
                logger.warn("[{}] Unrecognized date format {}T{}", currentUser, endDate, endTime);
                responseMap.put(errorMessage, "Unrecognized date or time format within end time");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }

            // Parsing divisions
            String[] divArray = invitedDivisions.split(",");
            for (String division : divArray)
            {
                Division div = divisionRepository.findByName(division);
                if (div == null)
                {
                    logger.warn("[{}] Unrecognized division {}", currentUser, division);
                    responseMap.put(errorMessage, "Division " + division + " does not exist");
                    return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
                } else
                {
                    event.addDivision(div);
                }
            }
            // Updating list of invited user after divisions changed
            event.updateInvitedUser(divisionRepository);
        }

        /*
            Parsing non-mandatory properties
         */

        event.setDescription(eventDescription);
        event.setLocation(location);

        if(!locationLat.isEmpty())
        {
            try
            {
                event.setLocationLat(Double.parseDouble(locationLat));
            } catch (NumberFormatException e)
            {
                logger.warn("[{}] Unable to parse lat {}", currentUser, locationLat);
                responseMap.put(errorMessage, "Unable to parse latitude coordinate");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        }

        if(!locationLng.isEmpty())
        {
            try
            {
                event.setLocationLng(Double.parseDouble(locationLng));
            } catch (NumberFormatException e)
            {
                logger.warn("[{}] Unable to parse lng {}", currentUser, locationLng);
                responseMap.put(errorMessage, "Unable to parse longitude coordinate");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        }

        event.setEventAdmin(currentUser);
        event.setLastChanged(LocalDateTime.now());

        /*
            Saving event
         */

        eventRepository.save(event);
        logger.info("[{}] Successfully saved event {}", currentUser, event);
        responseMap.put(successMessage, "Successfully saved the event");
        responseMap.put(eventId, event.getId());
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    /**
     * This function deletes an event, specified by the event ID. The function is invoked by DELETEing the URI /api/admin/event.
     * @param eventId The ID of the event, that should be deleted.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteEvent(@RequestParam(value = "id") String eventId, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Deleting event with id {}", currentUser, eventId);
        Event event;
        if(eventId.isEmpty())
        {
            logger.warn("[{}] The id of an event is not allowed to be empty", currentUser);
            return new ResponseEntity<>("The ID of an event is not allowed to be empty", HttpStatus.BAD_REQUEST);
        } else if((event = eventRepository.findById(eventId)) == null)
        {
            logger.warn("[{}] Unable to find event with id {}", currentUser, eventId);
            return new ResponseEntity<>("Unable to find the specified event", HttpStatus.BAD_REQUEST);
        } else if(!currentUser.isAllowedToAdministrate(event))
        {
            logger.warn("[{}] The user is not allowed to modify the event owned by {}", currentUser, event.getEventAdmin());
            return new ResponseEntity<>("You are not allowed to modify the selected event", HttpStatus.FORBIDDEN);
        } else
        {
            eventRepository.delete(event);
            logger.info("[{}] Successfully delete the selected event {}", currentUser, event);
            return new ResponseEntity<>("Successfully deleted selected event", HttpStatus.OK);
        }
    }
}
