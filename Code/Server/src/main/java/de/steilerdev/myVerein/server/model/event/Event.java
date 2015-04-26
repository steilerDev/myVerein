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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.steilerdev.myVerein.server.model.BaseEntity;
import de.steilerdev.myVerein.server.model.division.Division;
import de.steilerdev.myVerein.server.model.division.DivisionHelper;
import de.steilerdev.myVerein.server.model.division.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This object is representing an entity within the division's collection of the MongoDB. On top of that the class is providing several useful helper methods.
 */
public class Event extends BaseEntity
{
    @Transient
    @JsonIgnore
    private final Logger logger = LoggerFactory.getLogger(Event.class);

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

    @Indexed
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime startDateTime;

    @Indexed
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime endDateTime;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String administrationNotAllowedMessage;

    @DBRef(lazy = true)
    @NotEmpty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Division> invitedDivision;

    @DBRef(lazy = true)
    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User eventAdmin;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EventStatus userResponse;

    /**
     * This variable holds the information when this object was last changed in a way that the invited user should update their cached information about the event.
     * This flag is updated every time the content is changed by an administrator or a user is removed (Because of a removed invited division or because the user was un-subscribed from a division)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime lastChanged;

    /*
        Constructors (Empty one to meet bean definition and convenience ones)
     */

    public Event() {}

    /*
        Mandatory getter and setter
     */

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

    public LocalDateTime getStartDateTime()
    {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime)
    {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime()
    {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime)
    {
        this.endDateTime = endDateTime;
    }

    public List<Division> getInvitedDivision()
    {
        return invitedDivision;
    }

    public void setInvitedDivision(List<Division> invitedDivision)
    {
        this.invitedDivision = invitedDivision;
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

    public EventStatus getUserResponse()
    {
        return userResponse;
    }

    public void setUserResponse(EventStatus userResponse)
    {
        this.userResponse = userResponse;
    }

    /*
        Convenience getter and setter
     */

    /**
     * This function updates the list of invited user of this event
     * @param divisionRepository An active division repository used to expand the division set
     */
    public void updateInvitedUser(DivisionRepository divisionRepository)
    {
        if(invitedDivision == null || (invitedDivision = DivisionHelper.getExpandedSetOfDivisions(invitedDivision, divisionRepository)) == null)
        {
            logger.error("Unable to update invited user, because invited divisions are null");
        }  else
        {
            logger.info("Updating invited user for event {}", this);
            Set<String> oldInvitedUser = invitedUser == null? new HashSet<>(): invitedUser.keySet();
            HashSet<String> newInvitedUser = new HashSet<>();
            invitedDivision.stream().forEach(div -> newInvitedUser.addAll(div.getMemberList()));

            if(oldInvitedUser.isEmpty() || newInvitedUser.isEmpty())
            {
                logger.debug("Old set of invited user or new set of invited user is empty");
                invitedUser = new HashMap<>();
                if (oldInvitedUser.isEmpty() && !newInvitedUser.isEmpty())
                {
                    logger.debug("Old set of invited user is empty and new set of invited user is not empty");
                    newInvitedUser.stream().forEach(userID -> invitedUser.put(userID, EventStatus.PENDING));
                } else if (newInvitedUser.isEmpty() && !oldInvitedUser.isEmpty())
                {
                    logger.debug("New set of invited user is empty and old set of invited user is not empty");
                    oldInvitedUser.stream().forEach(userID -> invitedUser.put(userID, EventStatus.REMOVED));
                    lastChanged = LocalDateTime.now();
                }
            } else
            {
                logger.debug("Old and new set of invited user is not empty");
                oldInvitedUser.removeAll(newInvitedUser);
                oldInvitedUser.stream().forEach(userID -> invitedUser.put(userID, EventStatus.REMOVED));
                newInvitedUser.stream().forEach(userID -> invitedUser.putIfAbsent(userID, EventStatus.PENDING));
                lastChanged = LocalDateTime.now();
            }
        }
    }

    /**
     * This function adds a division to the list of invited division. After all divisions got update, {@link #updateInvitedUser(DivisionRepository)} should get called to update the list of invited user.
     * @param division The division that should be added to the list of invited division.
     */
    public void addDivision(Division division)
    {
        if(invitedDivision == null)
        {
            invitedDivision = new ArrayList<>();
        }
        invitedDivision.add(division);
    }

    /**
     * Sets the response of the user to 'going'. This function does not check if the user was invited in the first place.
     * @param user The user, whose response is going to get set.
     */
    public void setGoing(User user)
    {
        invitedUser.put(user.getId(), EventStatus.GOING);
    }

    /**
     * Sets the response of the user to 'decline'. This function does not check if the user was invited in the first place.
     * @param user The user, whose response is going to get set.
     */
    public void setDecline(User user)
    {
        invitedUser.put(user.getId(), EventStatus.DECLINE);
    }

    /**
     * Sets the response of the user to 'maybe'. This function does not check if the user was invited in the first place.
     * @param user The user, whose response is going to get set.
     */
    public void setMaybe(User user)
    {
        invitedUser.put(user.getId(), EventStatus.MAYBE);
    }

    /**
     * Sets the response of the user to 'removed'. This function does not check if the user was invited in the first place.
     * @param user The user, whose response is going to get set.
     */
    public void setRemoved(User user)
    {
        invitedUser.put(user.getId(), EventStatus.REMOVED);
    }

    /*
        Sending object functions
     */

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
        Event sendingObject = new Event();
        sendingObject.setId(id);
        sendingObject.setName(name);
        sendingObject.setStartDateTime(startDateTime);
        sendingObject.setEndDateTime(endDateTime);
        return sendingObject;
    }

    /**
     * This function removes all fields that the other users of the app are not allowed to see.
     * @param receivingUser The user who is receiving the event.
     * @return A copied message object, without the fields, other users are not allowed to see.
     */
    @JsonIgnore
    @Transient
    public Event getSendingObjectInternalSync(User receivingUser)
    {
        Event sendingObject = getSendingObject();
        sendingObject.userResponse = invitedUser.get(receivingUser.getId());
        return sendingObject;
    }

    /**
     * This function creates a sending-save object (ensuring there is no infinite loop caused by references). No event admin is specified, but the name and id of every invited division is available.
     * @return A sending-save instance of the object.
     */
    @JsonIgnore
    @Transient
    public Event getSendingObjectWithDivisionNamesForWeb()
    {
        Event sendingObject = getSendingObject(new String[0]);

        if(sendingObject.getEventAdmin() != null)
        {
            sendingObject.setEventAdmin(sendingObject.getEventAdmin().getSendingObjectOnlyId());
        }

        if(sendingObject.getInvitedDivision() != null)
        {
            sendingObject.getInvitedDivision().replaceAll(Division::getSendingObjectOnlyIdAndName);
        }

        sendingObject.setLastChanged(null);

        return sendingObject;
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

        sendingObject.setLastChanged(null);

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

    /*
        Required java object functions
     */

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
}
