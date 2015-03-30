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
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalUnit;
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
    EventRepository eventRepository;

    @Autowired
    DivisionRepository divisionRepository;

    private static Logger logger = LoggerFactory.getLogger(EventManagementController.class);

    /**
     * This function gathers all dates where an event takes place within a specific month and year. The function is invoked by GETting the URI /api/admin/event/month and specifying the month and year via the request parameters.
     * @param month The selected month.
     * @param year The selected year.
     * @return An HTTP response with a status code. If the function succeeds, a list of dates, during the month, that contain an event, is returned, otherwise an error code is returned.
     */
    @RequestMapping(value = "month", produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<LocalDate>> getEventDatesOfMonth(@RequestParam String month,
                                                                @RequestParam String year,
                                                                @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Gathering events of month " + month + " and year " + year);
        ArrayList<LocalDate> dates = new ArrayList<>();
        int monthInt, yearInt;
        try
        {
            monthInt = Integer.parseInt(month);
            yearInt = Integer.parseInt(year);
        } catch (NumberFormatException e)
        {
            logger.warn("[" + currentUser + "] Unable to parse month or year.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        //Getting all single date events
        dates.addAll(eventRepository.findEventsByStartDateMonthAndStartDateYearAndMultiDate(monthInt, yearInt, false).parallelStream().map(event -> event.getStartDateTime().toLocalDate()).collect(Collectors.toList()));

        //Collecting all multi date events, that either start or end within the selected month (which means that events that are spanning over several months are not collected)
        Stream.concat(eventRepository.findEventsByStartDateMonthAndStartDateYearAndMultiDate(monthInt, yearInt, true).stream(), //All multi date events starting within the month
                      eventRepository.findEventsByEndDateMonthAndEndDateYearAndMultiDate(monthInt, yearInt, true).stream()) //All multi date events ending within the month
                        .distinct() //Removing all duplicated events
                        .forEach(event -> { //Creating a local date for each occupied date of the event
                            for (LocalDate date = event.getStartDateTime().toLocalDate(); //Starting with the start date
                                 !date.equals(event.getEndDateTime().toLocalDate()); //Until reaching the end date
                                 date = date.plusDays(1)) //Adding a day within each iterations
                            {
                                dates.add(date);
                            }
                            dates.add(event.getEndDateTime().toLocalDate()); //Finally adding the last date
                        });

        if(dates.isEmpty())
        {
            logger.warn("[" + currentUser + "] Returning empty dates list of " + month + "/" + year);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            logger.debug("[" + currentUser + "] Returning dates list " + month + "/" + year);
            return new ResponseEntity<>(dates.stream().distinct().collect(Collectors.toList()), HttpStatus.OK); //Returning an optimized set of events
        }
    }

    /**
     * Returns all events, that are taking place on a specified date. The date parameter needs to be formatted according to the following pattern: YYYY/MM/DD. This function is invoked by GETting the URI /api/admin/event/date
     * @param date The selected date, correctly formatted (YYYY/MM/DD)
     * @return An HTTP response with a status code. If the function succeeds, a list of events is returned, otherwise an error code is returned.
     */
    @RequestMapping(value = "date", produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getEventsOfDate(@RequestParam String date, @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Getting events of date " + date);
        LocalDate dateObject;
        ArrayList<Event> eventsOfDay = new ArrayList<>();
        try
        {
            dateObject = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            logger.debug("[" + currentUser + "] Converted to date object: " + dateObject.toString());
        } catch (DateTimeParseException e)
        {
            logger.warn("[" + currentUser + "] Unable to parse date: " + date + ": " + e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        eventsOfDay.addAll(eventRepository.findEventsByStartDateDayOfMonthAndStartDateMonthAndStartDateYearAndMultiDate(dateObject.getDayOfMonth(), dateObject.getMonthValue(), dateObject.getYear(), false));

        //Collecting all multi date events, that either start or end within the selected month (which means that events that are spanning over several months are not collected)
        eventsOfDay.addAll(Stream.concat(eventRepository.findEventsByStartDateMonthAndStartDateYearAndMultiDate(dateObject.getMonthValue(), dateObject.getYear(), true).stream(), //All multi date events starting within the month
                eventRepository.findEventsByEndDateMonthAndEndDateYearAndMultiDate(dateObject.getMonthValue(), dateObject.getYear(), true).stream()) //All multi date events ending within the month
                .distinct() //Removing all duplicated events
                .filter(event -> event.getStartDate().isEqual(dateObject) || event.getEndDate().isEqual(dateObject) || (event.getEndDate().isAfter(dateObject) && event.getStartDate().isBefore(dateObject))) //Filter all multi date events that do not span over the date
                .collect(Collectors.toList()));

        if(eventsOfDay.isEmpty())
        {
            logger.warn("[" + currentUser + "] The events list of " + date + " is empty");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            eventsOfDay.replaceAll(Event::getSendingObjectOnlyNameTimeId);
            logger.debug("[" + currentUser + "] Returning " + eventsOfDay.size() + " events for " + date);
            return new ResponseEntity<>(eventsOfDay, HttpStatus.OK);
        }
    }

    /**
     * Returns a specific event based on its id. The function is invoked by GETting the URI the URI /api/admin/event.
     * @param id The id of the selected event.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If the function succeeds, the event is returned, otherwise an error code is returned.
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<Event> getEvent(@RequestParam String id, @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Getting the event for ID " + id);
        if(id.isEmpty())
        {
            logger.warn("[" + currentUser + "] The ID can not be empty.");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            Event selectedEvent = eventRepository.findEventById(id);
            if (selectedEvent == null)
            {
                logger.warn("[" + currentUser + "] Unable to find specified event.");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } else
            {
                selectedEvent = selectedEvent.getSendingObjectInternalSync();
                if (!currentUser.isAllowedToAdministrate(selectedEvent))
                {
                    logger.debug("[" + currentUser + "] The user is not allowed to edit the event " + id);
                    selectedEvent.setAdministrationNotAllowedMessage("You are not allowed to modify this event, since you did not create it.");
                }
                return new ResponseEntity<>(selectedEvent, HttpStatus.OK);
            }
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
     * @return An HTTP response with a status code together with a JSON map object, containing an 'errorMessage', or a 'successMessage' respectively. If the operation was successful the id of the event is accessible via 'eventID'.
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
        logger.trace("[" + currentUser + "] Saving event");
        Map<String, String> responseMap = new HashMap<>();
        Event event;
        if(eventFlag.isEmpty())
        {
            logger.warn("[" + currentUser + "] The event flag is empty");
            responseMap.put("errorMessage", "The event flag is not allowed to be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(eventFlag.equals("true"))
        {
            logger.debug("[" + currentUser + "] A new event is created");
            event = new Event();
        } else
        {
            logger.debug("[" + currentUser + "] The event with id " + eventFlag + " is altered");
            event = eventRepository.findEventById(eventFlag);
            if(event == null)
            {
                logger.warn("[" + currentUser + "] Unable to find the specified event with id " + eventFlag);
                responseMap.put("errorMessage", "Unable to find the specified event");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            } else if(!currentUser.isAllowedToAdministrate(event))
            {
                logger.warn("[" + currentUser + "] The user is not allowed to alter the selected event " + eventFlag);
                responseMap.put("errorMessage", "You are not allowed to edit the selected event");
                return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
            }
        }

        event.setName(eventName);
        event.setDescription(eventDescription);

        if(startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty())
        {
            logger.warn("[" + currentUser + "] The date and times defining the event (ID " + eventFlag + ") are not allowed to be empty.");
            responseMap.put("errorMessage", "The date and times defining the event are not allowed to be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/y'T'H:m");
            try
            {
                event.setStartDateTime(LocalDateTime.parse(startDate + "T" + startTime, formatter));
            } catch (DateTimeParseException e)
            {
                logger.warn("[" + currentUser + "] Unrecognized date format " + startDate + "T" + startTime);
                responseMap.put("errorMessage", "Unrecognized date or time format within start time");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }

            try
            {
                event.setEndDateTime(LocalDateTime.parse(endDate + "T" + endTime, formatter));
            } catch (DateTimeParseException e)
            {
                logger.warn("[" + currentUser + "] Unrecognized date format " + endDate + "T" + endTime);
                responseMap.put("errorMessage", "Unrecognized date or time format within end time");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        }

        event.setLocation(location);

        if(!locationLat.isEmpty())
        {
            try
            {
                event.setLocationLat(Double.parseDouble(locationLat));
            } catch (NumberFormatException e)
            {
                logger.warn("[" + currentUser + "] Unable to paste lat " + locationLat);
                responseMap.put("errorMessage", "Unable to parse latitude coordinate");
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
                logger.warn("[" + currentUser + "] Unable to paste lng " + locationLng);
                responseMap.put("errorMessage", "Unable to parse longitude coordinate");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        }


        if (!invitedDivisions.isEmpty())
        {
            String[] divArray = invitedDivisions.split(",");
            for (String division : divArray)
            {
                Division div = divisionRepository.findByName(division);
                if (div == null)
                {
                    logger.warn("[" + currentUser + "] Unrecognized division (" + division + ")");
                    responseMap.put("errorMessage", "Division " + division + " does not exist");
                    return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
                }
                event.addDivision(div);
            }
            event.updateInvitedUser(divisionRepository);
        } else if(event.getInvitedDivision() != null && !event.getInvitedDivision().isEmpty())
        {
            event.updateInvitedUser(divisionRepository);
        }

        //Updating several fields.
        event.setEventAdmin(currentUser);
        event.setLastChanged(LocalDateTime.now());
        event.updateMultiDate();

        try
        {
            eventRepository.save(event);
            logger.info("[" + currentUser + "] Successfully saved event " + eventFlag);
            responseMap.put("successMessage", "Successfully saved the event");
            responseMap.put("eventID", event.getId());
            return new ResponseEntity<>(responseMap, HttpStatus.OK);
        } catch (ConstraintViolationException e)
        {
            logger.warn("[" + currentUser + "] A database constraint was violated while saving the event " + eventFlag);
            responseMap.put("errorMessage", "A database constraint was violated while saving the event");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This function deletes an event, specified by the event ID. The function is invoked by DELETEing the URI /api/admin/event.
     * @param id The ID of the event, that should be deleted.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteEvent(@RequestParam String id, @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Deleting event with id " + id);
        if(id.isEmpty())
        {
            logger.warn("[" + currentUser + "] The id of an event is not allowed to be empty.");
            return new ResponseEntity<>("The ID of an event is not allowed to be empty", HttpStatus.BAD_REQUEST);
        }

        Event event = eventRepository.findEventById(id);

        if(event == null)
        {
            logger.warn("[" + currentUser + "] Unable to find the selected event with id " + id);
            return new ResponseEntity<>("Unable to find the selected event", HttpStatus.BAD_REQUEST);
        } else if(!currentUser.isAllowedToAdministrate(event))
        {
            logger.warn("[" + currentUser + "] The user is not allowed to modify the event owned by " + event.getEventAdmin());
            return new ResponseEntity<>("You are not allowed to modify the selected event", HttpStatus.FORBIDDEN);
        } else
        {
            try
            {
                eventRepository.delete(event);
                logger.info("[" + currentUser + "] Successfully delete the selected event");
                return new ResponseEntity<>("Successfully deleted selected event", HttpStatus.OK);
            } catch (IllegalArgumentException e)
            {
                logger.warn("[" + currentUser + "] Unable to delete selected event: " + e.getMessage());
                return new ResponseEntity<>("Unable to delete the selected event", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
