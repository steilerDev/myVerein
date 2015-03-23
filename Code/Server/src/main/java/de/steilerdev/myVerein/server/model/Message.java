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
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String content;

    @NotNull
    private LocalDateTime timestamp;

    @NotNull
    @JsonIgnore
    private Map<String, MessageStatus> receiver;

    @DBRef
    @NotNull
    private User sender;

    @DBRef
    @NotNull
    private Division group;

    public Message() {}

    public Message(String content, LocalDateTime timestamp, User sender, List<User> receivers, Division group)
    {
        this.content = content;
        this.timestamp = timestamp;
        this.sender = sender;
        this.group = group;

        this.receiver = new HashMap<>();
        for(User user: receivers)
        {
            this.receiver.put(user.getId(), MessageStatus.PENDING);
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

    public void prepareForSending()
    {
        sender.removeEverythingExceptId();
        group.removeEverythingExceptId();
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
