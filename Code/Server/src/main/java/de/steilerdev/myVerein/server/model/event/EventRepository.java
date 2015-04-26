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
package de.steilerdev.myVerein.server.model.event;

import de.steilerdev.myVerein.server.model.division.Division;
import de.steilerdev.myVerein.server.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This interface is used to query the database for specific event object. The repository is implemented during runtime by SpringData.
 */
public interface EventRepository extends MongoRepository<Event, String> {

    List<Event> findByInvitedDivision(Division invitedDivision);
    Event findById(String id);

    /**
     * This function gathers all events spanning over a period specified by a provided start and end time.
     * @param periodStart The start date time of the period.
     * @param periodEnd The end date time of the period.
     * @return All events, who are (according to their startDateTime and endDateTime) spanning over the specified period.
     */
    @Query(value = "{$or: [" +
                        "{$and: [" + // This block is matching all events that take place between the period
                            "{'startDateTime': {$gte: ?0}}," + // Start date after start of period
                            "{'startDateTime': {$lte: ?1}}," + // Start date before end of period
                            "{'endDateTime': {$gte: ?0}}," + // End date after start of period
                            "{'endDateTime': {$lte: ?1}}" + // End date before end of period
                        "]}," +
                        "{$and: [" + // This block is matching all events that span over the start of the period
                            "{'startDateTime': {$lt: ?0}}," + // Start date before start of period
                            "{'endDateTime': {$gt: ?0}}" + // End date after start of period
                        "]}," +
                        "{$and: [" + // This block is matching all events that span over the end of the period
                            "{'startDateTime': {$lt: ?1}}," + // Start date before end of period
                            "{'endDateTime': {$gt: ?1}}" + // End date after end of period
                        "]}" +
                    "]}"
    )
    List<Event> findBySpanningOverPeriod(LocalDateTime periodStart, LocalDateTime periodEnd);

    /**
     * This function gathers all events, where the user is in the list of invited user.
     * @param prefixedUserID A prefixed user id, created by {@link EventHelper#prefixedUserIDForUser(User)}.
     * @return A list of events, where the user is in the list of invited user.
     */
    @Query(value = "{?0 : { $exists: true } }")
    List<Event> findByPrefixedInvitedUser(String prefixedUserID);

    /**
     * This function gathers all events, where the user is in the list of invited user and the {@link Event#lastChanged} flag is older than the specified time.
     * @param prefixedUserID A prefixed user id, created by {@link EventHelper#prefixedUserIDForUser(User)}.
     * @param lastChanged The last changed flag used to filter the events.
     * @return A list of events, where the use is in the list of invited user and that changed after the specified time.
     */
    @Query(value = "{$and: [{?0 : { $exists: true }}, {'lastChanged': {$gt: ?1}}] }")
    List<Event> findByPrefixedInvitedUserAndLastChangedAfter(String prefixedUserID, LocalDateTime lastChanged);
}
