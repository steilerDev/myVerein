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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.steilerdev.myVerein.server.controller.admin.DivisionManagementController;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This object is representing an entity within the division's collection of the MongoDB. On top of that the class is providing several useful helper methods.
 */
public class Event
{
    /**
     * This enum is representing the status of a message sent to a specific receiver.
     */
    public enum EventStatus {
        /**
         * This status is assigned to an event which has not received any answer from a particular user
         */
        PENDING {
            @Override
            public String toString() {
                return "PENDING";
            }
        },
        /**
         * This status is assigned to an event where the user stated he would participate
         */
        GOING {
            @Override
            public String toString() {
                return "GOING";
            }
        },
        /**
         * This status is assigned to an event where the user stated he might participate
         */
        MAYBE {
            @Override
            public String toString() {
                return "MAYBE";
            }
        },
        /**
         * This status is assigned to an event where the user stated he is not participating
         */
        DECLINE {
            @Override
            public String toString() {
                return "DECLINE";
            }
        },
        /**
         * This status is assigned to an event where the user was previously invited, but left the division
         */
        REMOVED {
            @Override
            public String toString() {
                return "DECLINE";
            }
        }
    }

    @Transient
    @JsonIgnore
    private static Logger logger = LoggerFactory.getLogger(Event.class);

    @Id
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String id;

    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String location;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private double locationLat;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private double locationLng;

    @JsonIgnore
    private Map<String, EventStatus> invitedUser;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime lastChanged;


    @Indexed
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime startDateTime;

    @Indexed
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime endDateTime;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean multiDate;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String administrationNotAllowedMessage;

    @DBRef
    @NotEmpty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Division> invitedDivision;

    @DBRef
    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User eventAdmin;

    public Event() {}

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description != null && !description.isEmpty()? description: null;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location != null && !location.isEmpty()? location: null;
    }

    public double getLocationLat()
    {
        return locationLat;
    }

    public void setLocationLat(double locationLat)
    {
        this.locationLat = locationLat;
    }

    public double getLocationLng()
    {
        return locationLng;
    }

    public void setLocationLng(double locationLng)
    {
        this.locationLng = locationLng;
    }

    public LocalDateTime getLastChanged()
    {
        return lastChanged;
    }

    public void setLastChanged(LocalDateTime lastChanged)
    {
        this.lastChanged = lastChanged;
    }

    /**
     * @return The LocalDateTime object of the start date of the event. The object is computed using the current startDate integers.
     */
    public LocalDateTime getStartDateTime()
    {

        return startDateTime;
    }

    /**
     * This function is setting the start date time and updates the startDate integers
     * @param startDateTime The new start date time.
     */
    public void setStartDateTime(LocalDateTime startDateTime)
    {
        this.startDateTime = startDateTime;
    }

    /**
     * @return The LocalDateTime object of the end date of the event. The object is computed using the current endDat integers.
     */
    public LocalDateTime getEndDateTime()
    {

        return endDateTime;
    }

    /**
     * This function is setting the end date time and updates the endDate integers
     * @param endDateTime The new start date time.
     */
    public void setEndDateTime(LocalDateTime endDateTime)
    {
        this.endDateTime = endDateTime;
    }

    @Transient
    @JsonIgnore
    public LocalDate getEndDate()
    {
        return endDateTime.toLocalDate();
    }

    @Transient
    @JsonIgnore
    public LocalDate getStartDate()
    {
        return startDateTime.toLocalDate();
    }

    public List<Division> getInvitedDivision()
    {
        return invitedDivision;
    }

    /**
     * The function is setting all invited divisions, but is optimizing the set by eliminating unnecessary divisions.
     * @param invitedDivision
     */
    public void setInvitedDivision(List<Division> invitedDivision)
    {
        this.invitedDivision = invitedDivision;
    }

    public void addDivision(Division division)
    {
        if(invitedDivision == null)
        {
            invitedDivision = new ArrayList<>();
        }
        invitedDivision.add(division);
    }

    public boolean isMultiDate()
    {
        return multiDate;
    }

    public void setMultiDate(boolean multiDate)
    {
        this.multiDate = multiDate;
    }

    /**
     * This function updates the multi date flag, depending on the start and end date.
     */
    public void updateMultiDate()
    {
        multiDate = !startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
    }


    public User getEventAdmin()
    {
        return eventAdmin;
    }

    public void setEventAdmin(User eventAdmin)
    {
        this.eventAdmin = eventAdmin;
    }

    public String getAdministrationNotAllowedMessage()
    {
        return administrationNotAllowedMessage;
    }

    public void setAdministrationNotAllowedMessage(String administrationNotAllowedMessage)
    {
        this.administrationNotAllowedMessage = administrationNotAllowedMessage;
    }

    public Map<String, EventStatus> getInvitedUser()
    {
        return invitedUser;
    }

    public void setInvitedUser(Map<String, EventStatus> invitedUser)
    {
        this.invitedUser = invitedUser;
    }

    public void setGoing(User user)
    {
        invitedUser.put(user.getId(), EventStatus.GOING);
    }

    public void setDecline(User user)
    {
        invitedUser.put(user.getId(), EventStatus.DECLINE);
    }

    public void setMaybe(User user)
    {
        invitedUser.put(user.getId(), EventStatus.MAYBE);
    }

    public void setRemoved(User user)
    {
        invitedUser.put(user.getId(), EventStatus.REMOVED);
    }

    /**
     * This function is removing unnecessary divisions from the invited division set.
     */
    public void optimizeInvitedDivisionSet(DivisionRepository divisionRepository)
    {
        if(invitedDivision != null && !invitedDivision.isEmpty())
        {
            invitedDivision = Division.getExpandedSetOfDivisions(invitedDivision, divisionRepository);
        }
    }

    /**
     * This function updates the list of invited user of this event
     * @param divisionRepository An active division repository used to expand the division set
     */
    public void updateInvitedUser(DivisionRepository divisionRepository)
    {
        if(invitedDivision == null || (invitedDivision = Division.getExpandedSetOfDivisions(invitedDivision, divisionRepository)) == null)
        {
            logger.error("Unable to update invited user, because invited divisions are null!");
        } else if (invitedUser == null)
        {
            invitedUser = new HashMap<>();
        } else
        {
            logger.info("Updating invited user for event " + this.getId());
            Set<String> oldInvitedUser = invitedUser.keySet();
            HashSet<String> newInvitedUser = new HashSet<>();
            invitedDivision.stream().forEach(div -> newInvitedUser.addAll(div.getMemberList()));

            if(oldInvitedUser.isEmpty() && newInvitedUser.isEmpty())
            {
                logger.debug("Old set of invited user and new set of invited user is empty");
                invitedUser = new HashMap<>();
            } else if(oldInvitedUser.isEmpty())
            {
                logger.debug("Old set of invited user is empty and new set of invited user is not empty");
                invitedUser = new HashMap<>();
                newInvitedUser.stream().forEach(userID -> invitedUser.put(userID, EventStatus.PENDING));
            } else if(newInvitedUser.isEmpty())
            {
                logger.debug("New set of invited user is empty and old set of invited user is not empty");
                invitedUser = new HashMap<>();
                oldInvitedUser.stream().forEach(userID -> invitedUser.put(userID, EventStatus.REMOVED));
            } else
            {
                logger.debug("Old and new set of invited user is not empty");
                oldInvitedUser.removeAll(newInvitedUser);
                oldInvitedUser.stream().forEach(userID -> invitedUser.put(userID, EventStatus.REMOVED));
                newInvitedUser.stream().forEach(userID -> invitedUser.putIfAbsent(userID, EventStatus.PENDING));
            }
        }
    }

    /**
     * This function creates a new event object and copies only the id of the current message.
     * @return A new message object only containing the id.
     */
    @JsonIgnore
    @Transient
    public Event getSendingObjectOnlyId()
    {
        Event sendingObject = new Event();
        sendingObject.setId(id);
        return sendingObject;
    }

    /**
     * This function creates a new message object only copies name time and id of the current message.
     * @return A new message object only containing the name, times and id.
     */
    @JsonIgnore
    @Transient
    public Event getSendingObjectOnlyNameTimeId()
    {
        String[] ignoredProperties = {
                "invitedDivision",
                "description",
                "location",
                "locationLat",
                "locationLng",
                "eventAdmin"
        };
        return getSendingObject(ignoredProperties);
    }

    /**
     * This function removes all fields that the other users of the app are not allowed to see.
     * @return A copied message object, without the fields, other users are not allowed to see.
     */
    @JsonIgnore
    @Transient
    public Event getSendingObjectInternalSync()
    {
        return getSendingObject();
    }

    /**
     * This function creates a sending-save object (ensuring there is no infinite loop caused by references)
     * @return A sending-save instance of the object.
     */
    @JsonIgnore
    @Transient
    public Event getSendingObject()
    {
        Event sendingObject = getSendingObject(new String[0]);

        if(sendingObject.getEventAdmin() != null)
        {
            sendingObject.setEventAdmin(sendingObject.getEventAdmin().getSendingObjectOnlyId());
        }

        if(sendingObject.getInvitedDivision() != null)
        {
            sendingObject.getInvitedDivision().replaceAll(Division::getSendingObjectOnlyId);
        }

        return sendingObject;
    }

    /**
     * This function copies the current object, ignoring the member fields specified by the ignored properties vararg.
     * @param ignoredProperties The member fields ignored during the copying.
     * @return A copy of the current object, not containing information about the ignored properties.
     */
    @JsonIgnore
    @Transient
    private Event getSendingObject(String... ignoredProperties)
    {
        Event sendingObject = new Event();
        BeanUtils.copyProperties(this, sendingObject, ignoredProperties);
        return sendingObject;
    }

    @Override
    public int hashCode()
    {
        return id == null? 0: id.hashCode();
    }

    /**
     * Equality is based on the same id String within the database.
     * @param obj The object compared to the current object.
     * @return True if the IDs of the two objects
     */
    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof Event && this.id != null && this.id.equals(((Event) obj).getId());
    }

    @Override
    public String toString()
    {
        return name != null && !name.isEmpty()? name: id;
    }

    /**
     * {@link de.steilerdev.myVerein.server.model.EventRepository#findAllByPrefixedInvitedUser(String)} needs a user id, prefixed with "invitedUser.", because a custom query with a fixed prefix is not working. This function creates this prefixed user id.
     * @param user The user, which needs to be prefixed.
     * @return The prefixed user ID.
     */
    public static String prefixedUserIDForUser(User user) {
        return user == null? null: "invitedUser." + user.getId();
    }
}
