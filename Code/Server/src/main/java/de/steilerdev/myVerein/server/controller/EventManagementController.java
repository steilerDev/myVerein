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
package de.steilerdev.myVerein.server.controller;

import de.steilerdev.myVerein.server.model.*;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This controller is processing all requests associated with the event management.
 */
@Controller
@RequestMapping("/event")
public class EventManagementController
{
    @Autowired
    EventRepository eventRepository;

    @Autowired
    DivisionRepository divisionRepository;

    private static Logger logger = LoggerFactory.getLogger(DivisionManagementController.class);

    /**
     * This function gathers all dates where an event takes place within a specific month and year.
     * @param month The selected month.
     * @param year The selected year.
     * @return A list of dates, during the month, that contain an event.
     */
    @RequestMapping(value = "getEventsOfMonth", produces = "application/json")
    public @ResponseBody List<LocalDate> getEventDatesOfMonth(@RequestParam String month,
                                                              @RequestParam String year)
    {
        ArrayList<LocalDate> dates = new ArrayList<>();
        int monthInt, yearInt;
        logger.debug("Gathering events of month " + month + " and year " + year);
        try
        {
            monthInt = Integer.parseInt(month);
            yearInt = Integer.parseInt(year);
        } catch (NumberFormatException e)
        {
            logger.warn("Unable to parse month or year.");
            return null;
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

        return dates.stream().distinct().collect(Collectors.toList()); //Returning an optimized set of events
    }

    /**
     * Returns all events, that are taking place on a specified date. The date needs to be formatted according to the following pattern: YYYY/MM/DD
     * @param date The selected date
     * @return A list of events meeting the stated requirements.
     */
    @RequestMapping(value = "getEventsOfDate", produces = "application/json")
    public @ResponseBody List<Event> getEventsOfDate(@RequestParam String date)
    {
        logger.debug("Getting events of date " + date);
        LocalDate dateObject;
        ArrayList<Event> eventsOfDay = new ArrayList<>();
        try
        {
            dateObject = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            logger.debug("Converted to date object: " + dateObject.toString());
        } catch (DateTimeParseException e)
        {
            logger.warn("Unable to parse date: " + date + ": " + e.toString());
            return null;
        }

        eventsOfDay.addAll(eventRepository.findEventsByStartDateDayOfMonthAndStartDateMonthAndStartDateYearAndMultiDate(dateObject.getDayOfMonth(), dateObject.getMonthValue(), dateObject.getYear(), false));

        //Collecting all multi date events, that either start or end within the selected month (which means that events that are spanning over several months are not collected)
        eventsOfDay.addAll(Stream.concat(eventRepository.findEventsByStartDateMonthAndStartDateYearAndMultiDate(dateObject.getMonthValue(), dateObject.getYear(), true).stream(), //All multi date events starting within the month
                eventRepository.findEventsByEndDateMonthAndEndDateYearAndMultiDate(dateObject.getMonthValue(), dateObject.getYear(), true).stream()) //All multi date events ending within the month
                .distinct() //Removing all duplicated events
                .filter(event -> event.getStartDate().isEqual(dateObject) || event.getEndDate().isEqual(dateObject) || (event.getEndDate().isAfter(dateObject) && event.getStartDate().isBefore(dateObject))) //Filter all multi date events that do not span over the date
                .collect(Collectors.toList()));

        eventsOfDay.stream().forEach(event -> {
            event.setInvitedDivision(null);
            event.setLocation(null);
            event.setDescription(null);
            event.setEventAdmin(null);
        });

        logger.debug("Returning " + eventsOfDay.size() + " events.");
        return eventsOfDay;
    }

    /**
     * Returns a specific event based on its id.
     * @param id The id of the selected event
     * @return The event.
     */
    @RequestMapping(value = "getEvent", produces = "application/json")
    public @ResponseBody Event getEvent(@RequestParam String id, @CurrentUser User currentUser)
    {
        if(id.isEmpty())
        {
            logger.warn("The ID can not be empty.");
            return null;
        } else
        {
            Event selectedEvent = eventRepository.findEventById(id);
            if (selectedEvent == null)
            {
                logger.warn("Unable to load specified element.");
                return null;
            } else
            {
                if (!(selectedEvent.getEventAdmin().equals(currentUser) || currentUser.getAuthorities().parallelStream().anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPERADMIN"))))
                {
                    selectedEvent.setAdministrationNotAllowedMessage("You are not allowed to modify this event, since you did not create it.");
                }

                //Preventing data loop
                if(selectedEvent.getInvitedDivision() != null)
                {
                    selectedEvent.getInvitedDivision().parallelStream().forEach(division -> division.setAdminUser(null));
                }
                if(selectedEvent.getEventAdmin().getDivisions() != null)
                {
                    selectedEvent.getEventAdmin().getDivisions().parallelStream().forEach(division -> division.setAdminUser(null));
                }
                return selectedEvent;
            }
        }
    }

    /**
     * Saves an event
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveEvent(@RequestParam String eventFlag,
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
        Event oldEvent = null,
              currentEvent;
        if(eventFlag.isEmpty())
        {
            logger.warn("The event flag is not allowed to be empty.");
            return new ResponseEntity<>("The event flag is not allowed to be empty", HttpStatus.BAD_REQUEST);
        } else if(eventFlag.equals("true"))
        {
            logger.debug("A new event is created.");
            currentEvent = new Event();
        } else
        {
            logger.debug("An existing event is altered.");
            oldEvent = currentEvent = eventRepository.findEventById(eventFlag);
            if(oldEvent == null)
            {
                logger.warn("Unable to find the specified event with id " + eventFlag);
                return new ResponseEntity<>("Unable to find the specified event", HttpStatus.BAD_REQUEST);
            } else if(oldEvent.getEventAdmin().equals(currentUser))
            {
                logger.warn("The current user " + currentUser.getEmail() + " is not allowed to alter the selected event " + eventFlag);
                return new ResponseEntity<>("You are not allowed to edit the selected event", HttpStatus.FORBIDDEN);
            }
        }

        currentEvent.setName(eventName);
        currentEvent.setDescription(eventDescription);

        if(startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty())
        {
            logger.warn("The date and times defining the event are not allowed to be empty.");
            return new ResponseEntity<>("The date and times defining the event are not allowed to be empty", HttpStatus.BAD_REQUEST);
        } else
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/y'T'H:m");
            try
            {
                currentEvent.setStartDateTime(LocalDateTime.parse(startDate + "T" + startTime, formatter));
            } catch (DateTimeParseException e)
            {
                logger.warn("Unrecognized date format " + startDate + "T" + startTime);
                return new ResponseEntity<>("Unrecognized date or time format within start time", HttpStatus.BAD_REQUEST);
            }

            try
            {
                currentEvent.setEndDateTime(LocalDateTime.parse(endDate + "T" + endTime, formatter));
            } catch (DateTimeParseException e)
            {
                logger.warn("Unrecognized date format " + endDate + "T" + endTime);
                return new ResponseEntity<>("Unrecognized date or time format within end time", HttpStatus.BAD_REQUEST);
            }
        }

        currentEvent.setLocation(location);

        try
        {
            currentEvent.setLocationLat(Double.parseDouble(locationLat));
        } catch (NumberFormatException e)
        {
            logger.warn("Unable to paste lat " + locationLat);
            return new ResponseEntity<>("Unable to parse latitude coordinate", HttpStatus.BAD_REQUEST);
        }

        try
        {
            currentEvent.setLocationLng(Double.parseDouble(locationLng));
        } catch (NumberFormatException e)
        {
            logger.warn("Unable to paste lng " + locationLng);
            return new ResponseEntity<>("Unable to parse longitude coordinate", HttpStatus.BAD_REQUEST);
        }

        if (!invitedDivisions.isEmpty())
        {
            String[] divArray = invitedDivisions.split(",");
            for (String division : divArray)
            {
                Division div = divisionRepository.findByName(division);
                if (div == null)
                {
                    logger.warn("Unrecognized division (" + div + ")");
                    return new ResponseEntity<>("Division " + div + " does not exist", HttpStatus.BAD_REQUEST);
                }
                currentEvent.addDivision(div);
            }
        }

        currentEvent.setEventAdmin(currentUser);
        currentEvent.setLastChanged(LocalDateTime.now());
        currentEvent.updateMultiDate();
        currentEvent.optimizeInvitedDivisionSet();

        try
        {
            eventRepository.save(currentEvent);
            if(oldEvent != null)
            {
                eventRepository.delete(oldEvent);
            }
        } catch (ConstraintViolationException e)
        {
            logger.warn("A database constraint was violated while saving the event " + eventFlag);
            return new ResponseEntity<>("A database constraint was violated while saving the event.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Successfully saved the event", HttpStatus.OK);
    }
}
