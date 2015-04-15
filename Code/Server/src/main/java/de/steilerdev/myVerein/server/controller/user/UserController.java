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


import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import de.steilerdev.myVerein.server.apns.PushService;
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
@RestController
@RequestMapping("/api/user/user")
public class UserController
{
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<User> getUser(@RequestParam(value = "id") String userID, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Loading user with ID {}", currentUser, userID);
        User searchedUser;
        if(userID.isEmpty())
        {
            logger.warn("[" + currentUser + "] The user ID is empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if ((searchedUser = userRepository.findById(userID)) == null)
        {
            logger.warn("[" + currentUser + "] Unable to find user with the stated ID " + userID);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            logger.info("[" + currentUser + "] Returning user with ID " + userID);
            return new ResponseEntity<>(searchedUser.getSendingObjectInternalSync(), HttpStatus.OK);
        }
    }

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

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity sendNotification(@CurrentUser User currentUser)
    {
        logger.info("Sending push to {}", currentUser);


        final byte[] token = currentUser.getDeviceToken();

        final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();

        payloadBuilder.setAlertBody("Ring ring, Neo.");

        final String payload = payloadBuilder.buildWithDefaultMaximumLength();

        final PushManager<SimpleApnsPushNotification> pushManager = PushService.getInstance();

        try
        {
            if(pushManager != null)
            {
                pushManager.getQueue().put(new SimpleApnsPushNotification(token, payload));
            }
        } catch (Exception e)
        {
            logger.error("Exception while sending: {}", e.getMessage());
        }
        logger.debug("Successfully send notification");

//        byte[] payload = APNS.newPayload().alertBody("Hello world").buildBytes();
//        if(currentUser.getDeviceToken() != null && payload != null)
//        {
//            logger.debug("Sending notification");
//            PushService.getInstance().push(currentUser.getDeviceToken(), payload);
            return new ResponseEntity(HttpStatus.OK);
//        } else {
//            logger.debug("Unable to send notification");
//            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
    }

}
