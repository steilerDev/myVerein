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

@RestController
@RequestMapping("/api/user/message")
public class MessageRestController
{
    private static Logger logger = LoggerFactory.getLogger(MessageRestController.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<Message>> getUnreadMessages(@CurrentUser User currentUser)
    {
        logger.trace("[Current User " + currentUser.getEmail() + "] Getting unread messages of " + currentUser.getEmail());
        List<Message> undeliveredMessages = messageRepository.findAllByPrefixedReceiverIDAndMessageStatus(Message.receiverIDForUser(currentUser), Message.MessageStatus.PENDING);
        if (undeliveredMessages == null)
        {
            logger.debug("[Current User " + currentUser.getEmail() + "] Unable to find any undelivered messages");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            undeliveredMessages.parallelStream().forEach(message -> message.setDelivered(currentUser));
            try
            {
                //messageRepository.save(undeliveredMessages);
                undeliveredMessages.parallelStream().forEach(message -> message.prepareForSending());
                logger.info("[Current User " + currentUser.getEmail() + "] Returning undelivered messages for " + currentUser.getEmail());
                return new ResponseEntity<>(undeliveredMessages, HttpStatus.OK);
            } catch (IllegalArgumentException e)
            {
                logger.warn("[Current User " + currentUser.getEmail() + "] Unable to save messages for " + currentUser.getEmail() + ": " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @RequestMapping(produces = "application/json", method = RequestMethod.POST)
    public ResponseEntity<String> sendMessage(@CurrentUser User currentUser, @RequestParam String division, @RequestParam String content)
    {
        Division receivingDivision;
        if(division.isEmpty() || content.isEmpty())
        {
            logger.warn("[Current User " + currentUser.getEmail() + "] Required parameters for sending message missing");
            return new ResponseEntity<>("Required parameter missing", HttpStatus.BAD_REQUEST);
        } else if ((receivingDivision = divisionRepository.findByName(division)) == null)
        {
            logger.warn("[Current User " + currentUser.getEmail() + "] Unable to find receiving division " + division);
            return new ResponseEntity<>("Unable to find receiving division", HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {

            List<User> receivingUser = userRepository.findAllByDivisionInDivisions(receivingDivision);

            if (receivingUser.isEmpty())
            {
                logger.warn("[Current User " + currentUser.getEmail() + "] Empty receiver list");
                return new ResponseEntity<>("Empty receiver list", HttpStatus.BAD_REQUEST);
            } else
            {
                Message message = new Message(content, LocalDateTime.now(), currentUser, receivingUser, receivingDivision);
                messageRepository.save(message);
                logger.info("[Current User " + currentUser.getEmail() + "] Successfully saved message");
                return new ResponseEntity<>("Successfully saved message", HttpStatus.OK);
            }
        }
    }
}
