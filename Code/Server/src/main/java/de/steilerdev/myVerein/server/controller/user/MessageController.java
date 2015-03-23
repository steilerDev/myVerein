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
package de.steilerdev.myVerein.server.controller.user;

import de.steilerdev.myVerein.server.model.*;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This class handles all messages send and received by a user.
 */
@RestController
@RequestMapping("/api/user/message")
public class MessageController
{
    private static Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * This function retrieves all unread messages of a user. The function is invoked bu GETting the URI /api/user/message.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code, together with the JSON list-object of all unread messages, or only an error code if an error occurred.
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<Message>> getMessages(@CurrentUser User currentUser, @RequestParam(required = false) String all)
    {
        logger.trace("[" + currentUser + "] Getting unread messages");
        List<Message> messages = messageRepository.findAllByPrefixedReceiverIDAndMessageStatus(Message.receiverIDForUser(currentUser), Message.MessageStatus.PENDING);
        if (messages == null)
        {
            logger.debug("[" + currentUser + "] Unable to find any undelivered messages");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            messages.parallelStream().forEach(message -> message.setDelivered(currentUser));

            if(all != null && !all.isEmpty())
            {
                logger.debug("Retrieving all messages");
                messages.addAll(messageRepository.findAllByPrefixedReceiverIDAndMessageStatus(Message.receiverIDForUser(currentUser), Message.MessageStatus.DELIVERED));
                messages.addAll(messageRepository.findAllByPrefixedReceiverIDAndMessageStatus(Message.receiverIDForUser(currentUser), Message.MessageStatus.READ));
            }

            try
            {
                messageRepository.save(messages);
                messages.parallelStream().forEach(Message::prepareForSending);
                logger.info("[" + currentUser + "] Returning undelivered messages for " + currentUser.getEmail());
                return new ResponseEntity<>(messages, HttpStatus.OK);
            } catch (IllegalArgumentException e)
            {
                logger.warn("[" + currentUser + "] Unable to save messages for " + currentUser.getEmail() + ": " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * This function sends a message to a specific group chat. The function is invoked by POSTing to the URI /api/user/message.
     * @param currentUser The currently logged in user.
     * @param division The name of the division where the message is send to.
     * @param content The content of the message.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.POST)
    public ResponseEntity<Message> sendMessage(@CurrentUser User currentUser, @RequestParam String division, @RequestParam String content)
    {
        logger.trace("[" + currentUser + "] Sending message to " + division);
        Division receivingDivision;
        if(division.isEmpty() || content.isEmpty())
        {
            logger.warn("[" + currentUser + "] Required parameters for sending message missing");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if ((receivingDivision = divisionRepository.findById(division)) == null)
        {
            logger.warn("[" + currentUser + "] Unable to find receiving division " + division);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if (!currentUser.getDivisions().contains(receivingDivision))
        {
            logger.warn("[" + currentUser + "] Trying to send a message to a division he is not part of: " + division);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            List<User> receivingUser = userRepository.findAllByDivisionInDivisions(receivingDivision);

            if (receivingUser.isEmpty())
            {
                logger.warn("[" + currentUser + "] Empty receiver list");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else
            {
                Message message = new Message(content, LocalDateTime.now(), currentUser, receivingUser, receivingDivision);
                messageRepository.save(message);
                logger.info("[" + currentUser + "] Successfully saved message");
                return new ResponseEntity<>(message, HttpStatus.OK);
            }
        }
    }
}
