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
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This object is representing an entity within the messages' collection of the MongoDB.
 */
public class Message
{
    /**
     * This enum is representing the status of a message sent to a specific receiver.
     */
    public enum MessageStatus {
        /**
         * This status is assigned to a message which is stored on the server but not delivered to the client yet
         */
        PENDING {
            @Override
            public String toString() {
                return "PENDING";
            }
        },
        /**
         * This status is assigned to a message which is delivered to the client
         */
        DELIVERED {
            @Override
            public String toString() {
                return "DELIVERED";
            }
        },
        /**
         * This status is assigned to a message which is read by the client
         */
        READ {
            @Override
            public String toString() {
                return "READ";
            }
        }
    }

    @Id
    private String id;

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

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

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

    public void setDelivered(User user)
    {
        receiver.put(user.getId(), MessageStatus.DELIVERED);
    }

    public void setRead(User user)
    {
        receiver.put(user.getId(), MessageStatus.READ);
    }

    public void setReceivingUser(User... users)
    {
        receiver = new HashMap<>();
        for(User user: users)
        {
            receiver.put(user.getId(), MessageStatus.PENDING);
        }
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

    /**
     * {@link de.steilerdev.myVerein.server.model.MessageRepository#findAllByPrefixedReceiverIDAndMessageStatus} needs a receiver id, prefixed with "receiver.", because a custom query with a fixed prefix is not working. This function creates this prefixed receiver id.
     * @param user The user, which needs to be prefixed.
     * @return The prefixed user ID.
     */
    public static String receiverIDForUser(User user) {
        return user == null? null: "receiver." + user.getId();
    }
}
