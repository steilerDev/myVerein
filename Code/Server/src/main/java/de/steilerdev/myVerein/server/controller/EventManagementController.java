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

import de.steilerdev.myVerein.server.model.Event;
import de.steilerdev.myVerein.server.model.EventRepository;
import de.steilerdev.myVerein.server.model.User;
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

import java.time.LocalDate;
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

    private static Logger logger = LoggerFactory.getLogger(DivisionManagementController.class);

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveEvent(@RequestParam String name,
                                        @RequestParam String oldName,
                                        @RequestParam String description,
                                        @RequestParam String admin,
                                        @CurrentUser User currentUser)
    {
        return new ResponseEntity<>("No", HttpStatus.BAD_REQUEST);
    }

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
     * @param dateString The selected date
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
                .filter(event -> event.getStartDate().isEqual(dateObject) || event.getEndDate().isEqual(dateObject) || (event.getEndDate().isBefore(dateObject) && event.getStartDate().isAfter(dateObject))) //Filter all multi date events that do not span over the date
                .collect(Collectors.toList()));
        logger.debug("Returning " + eventsOfDay.size() + " events.");
        return eventsOfDay;
    }
}
