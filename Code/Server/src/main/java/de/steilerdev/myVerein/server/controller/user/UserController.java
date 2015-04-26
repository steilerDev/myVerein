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

import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.model.UserRepository;
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

/**
 * This class handles gathering and updating information about a user by another/the same user.
 */
@RestController
@RequestMapping("/api/user/user")
public class UserController
{
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * This function gathers and returns the user identified by its id, containing all information the requesting user is allowed to see. The function is invoked by GETting the URI /api/user/user with the parameter id.
     * @param userID The user id of the requested user.
     * @param currentUser The currently logged in user.
     * @return A response entity either containing the user and a success code, or a failure code.
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<User> getUser(@RequestParam(value = "id") String userID, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Loading user with ID {}", currentUser, userID);
        User searchedUser;
        if(userID.isEmpty())
        {
            logger.warn("[{}] The user ID is empty", currentUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if ((searchedUser = userRepository.findById(userID)) == null)
        {
            logger.warn("[{}] Unable to find user with the stated ID {}", currentUser, userID);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            logger.info("[{}] Returning user {}", currentUser, searchedUser);
            return new ResponseEntity<>(searchedUser.getSendingObjectInternalSync(), HttpStatus.OK);
        }
    }

    /**
     * This function is used to update the device token of a user, who has opted-in to receive push notifications. The function is invoked by POSTing the deviceToken parameter to the URI /api/user/user.
     * @param deviceToken The Base64 encoded device token string.
     * @param currentUser The currently logged in user.
     * @return A response entity containing a response code, reflecting the result of the operation.
     */
    @RequestMapping(method = RequestMethod.POST, params = "deviceToken")
    public ResponseEntity updateDeviceToken(@RequestParam String deviceToken, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Updating device token", currentUser);
        if(deviceToken.isEmpty())
        {
            logger.warn("[{}] The device token is empty", currentUser);
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else
        {
            if(currentUser.setDeviceTokenBase64Encoded(deviceToken))
            {
                logger.info("[{}] Successfully updated device token", currentUser);
                userRepository.save(currentUser);
                return new ResponseEntity(HttpStatus.OK);
            } else
            {
                logger.warn("[{}] Unable to update device token, decoded token does not have the correct length of 32 bytes", currentUser);
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        }
    }
}
