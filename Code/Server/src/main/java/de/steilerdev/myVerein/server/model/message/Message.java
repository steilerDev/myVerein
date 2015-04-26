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
package de.steilerdev.myVerein.server.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.steilerdev.myVerein.server.model.BaseEntity;
import de.steilerdev.myVerein.server.model.user.User;
import de.steilerdev.myVerein.server.model.division.Division;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This object is representing an entity within the messages' collection of the MongoDB.
 */
public class Message extends BaseEntity
{
    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String content;

    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime timestamp;

    @NotNull
    @JsonIgnore
    private Map<String, MessageStatus> receiver;

    @DBRef(lazy = true)
    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User sender;

    @DBRef(lazy = true)
    @NotNull
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Division group;

    /*
        Constructors (Empty one to meet bean definition and convenience ones)
     */

    public Message() {}

    public Message(String content, User sender, Division group)
    {
        this(content, LocalDateTime.now(), sender, group);
    }

    public Message(String content, LocalDateTime timestamp, User sender, Division group)
    {
        this.content = content;
        this.timestamp = timestamp;
        this.sender = sender;
        this.group = group;

        this.receiver = new HashMap<>();
        for(String userID: group.getMemberList())
        {
            this.receiver.put(userID, MessageStatus.PENDING);
        }
        setDelivered(sender);
    }

    /*
        Mandatory basic getter and setter
     */

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
    }

    public User getSender()
    {
        return sender;
    }

    public void setSender(User sender)
    {
        this.sender = sender;
    }

    public Map<String, MessageStatus> getReceiver()
    {
        return receiver;
    }

    public void setReceiver(Map<String, MessageStatus> receiver)
    {
        this.receiver = receiver;
    }

    public Division getGroup()
    {
        return group;
    }

    public void setGroup(Division group)
    {
        this.group = group;
    }

    /*
        Convenience getter and setter
     */

    /**
     * This function sets the status of the message for the specified user to delivered. This function does not check if the user is part of the receiver list.
     * @param user The user that received the message.
     */
    public void setDelivered(User user)
    {
        receiver.put(user.getId(), MessageStatus.DELIVERED);
    }

    /**
     * This function sets the status of the message for the specified user to read. This function does not check if the user is part of the receiver list.
     * @param user The user that read the message.
     */
    public void setRead(User user)
    {
        receiver.put(user.getId(), MessageStatus.READ);
    }

    /**
     * This function replaces the list of receiving user by the specified user. The status is set to 'pending' by default.
     * @param users
     */
    public void setReceivingUser(User... users)
    {
        receiver = new HashMap<>();
        for(User user: users)
        {
            receiver.put(user.getId(), MessageStatus.PENDING);
        }
    }

    /*
        Sending object functions
     */

    /**
     * This function creates a new message object and copies only the id and timestamp of the current message. This is used as a response to a succesfully send message.
     * @return A new message object only containing the id and timestamp.
     */
    @JsonIgnore
    @Transient
    public Message getSendingObjectOnlyIdAndTimestamp()
    {
        Message sendingObject = new Message();
        sendingObject.setId(id);
        sendingObject.setTimestamp(timestamp);
        return sendingObject;
    }

    /**
     * This function creates a new message object and copies only the id of the current message.
     * @return A new message object only containing the id.
     */
    @JsonIgnore
    @Transient
    public Message getSendingObjectOnlyId()
    {
        Message sendingObject = new Message();
        sendingObject.setId(id);
        return sendingObject;
    }

    /**
     * This function removes all fields that the other users of the app are not allowed to see.
     * @return A copied message object, without the fields, other users are not allowed to see.
     */
    @JsonIgnore
    @Transient
    public Message getSendingObjectInternalSync()
    {
        return getSendingObject();
    }

    /**
     * This function creates a sending-save object (ensuring there is no infinite loop caused by references)
     * @return A sending-save instance of the object.
     */
    @JsonIgnore
    @Transient
    public Message getSendingObject()
    {
        Message sendingObject = getSendingObject(new String[0]);
        if(sendingObject.getSender() != null)
        {
            sendingObject.setSender(sender.getSendingObjectOnlyId());
        }

        if(sendingObject.getGroup() != null)
        {
            sendingObject.setGroup(group.getSendingObjectOnlyId());
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
    private Message getSendingObject(String... ignoredProperties)
    {
        Message sendingObject = new Message();
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

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof Message && this.id != null && this.id.equals(((Message) obj).getId());
    }

    @Override
    public String toString()
    {
        if (content != null && !content.isEmpty() && group != null)
        {
            return content + " send to " + group;
        } else if(content != null && !content.isEmpty())
        {
            return content;
        } else
        {
            return id;
        }
    }
}
