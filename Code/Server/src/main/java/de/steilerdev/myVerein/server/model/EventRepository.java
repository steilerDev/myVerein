/**
 * Copyright (C) 2014 Frank Steiler <frank@steilerdev.de>
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
package de.steilerdev.myVerein.server.model;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This interface is used to query the database for specific event object. The repository is implemented during runtime by SpringData through the @Repository annotation.
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    public List<Event> findEventsByStartDateMonthAndStartDateYearAndMultiDate(int startDateMonth, int startDateYear, boolean multiDate);
    public List<Event> findEventsByEndDateMonthAndEndDateYearAndMultiDate(int endDateMonth, int endDateYear, boolean multiDate);

    public List<Event> findEventsByStartDateDayOfMonthAndStartDateMonthAndStartDateYearAndMultiDate(int startDateDayOfMonth, int startDateMonth, int startDateYear, boolean multiDate);

    public Event findEventById(String id);
}
