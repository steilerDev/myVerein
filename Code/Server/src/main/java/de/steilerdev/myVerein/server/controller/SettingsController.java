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
package de.steilerdev.myVerein.server.controller;

import com.mongodb.MongoException;
import de.steilerdev.myVerein.server.model.GridFSRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.model.UserRepository;
import de.steilerdev.myVerein.server.security.CurrentUser;
import de.steilerdev.myVerein.server.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class handles all requests done through the settings page
 */
@Controller
@RequestMapping("/settings")
public class SettingsController
{
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GridFSRepository gridFSRepository;

    private static Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    public void settings()
    {

    }

    /**
     * This request mapping is processing the request to get the current settings of the system.
     * @return The path to the view for the index page.
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public @ResponseBody Properties loadSettings(@CurrentUser User currentUser)
    {
        logger.debug("Loading settings for " + currentUser.getEmail());
        Properties settings;
        try
        {
            logger.debug("Loading properties file from classpath");
            settings = PropertiesLoaderUtils.loadProperties(new ClassPathResource("myVerein.properties"));
        } catch (IOException e)
        {
            logger.warn("Unable to load the system settings");
            return null;
        }

        if(settings == null)
        {
            logger.warn("Unable to load system settings.");
            return null;
        } else if(!currentUser.isAdmin())
        {
            logger.warn("A non-admin (" + currentUser.getEmail() + ") tries to access the settings");
            return null;
        } else if(!currentUser.isSuperAdmin())
        {
            settings.put("administrationNotAllowedMessage", "You are not the super admin, and therefore you cannot adjust system settings.");
        }

        currentUser.removeEverythingExceptEmailAndName();
        settings.put("currentAdmin", currentUser);

        return settings;
    }

    /**
     * This request mapping is processing the request to get the current settings of the system.
     * @return The path to the view for the index page.
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveSettings(@RequestParam(required = false) String currentAdmin,
                                                             @RequestParam(required = false) String adminPasswordNew,
                                                             @RequestParam(required = false) String adminPasswordNewRe,
                                                             @RequestParam(required = false) String clubName,
                                                             @RequestParam(required = false) MultipartFile clubLogo,
                                                             @RequestParam(required = false) String databaseHost,
                                                             @RequestParam(required = false) String databasePort,
                                                             @RequestParam(required = false) String databaseUser,
                                                             @RequestParam(required = false) String databasePassword,
                                                             @RequestParam(required = false) String databaseCollection,
                                                             @RequestParam(required = false) String rememberMeTokenKey,
                                                             @RequestParam String currentAdminPassword,
                                                             @CurrentUser User currentUser)
    {
        if(!passwordEncoder.isPasswordValid(currentUser.getPassword(), currentAdminPassword, currentUser.getSalt()))
        {
            logger.warn("The stated password of user " + currentUser.getEmail() + " is invalid");
            return new ResponseEntity<>("The stated password is incorrect, please try again", HttpStatus.FORBIDDEN);
        } else if(currentUser.isAdmin())
        {
            logger.debug("The user is an admin");
            if(currentAdmin != null && !currentAdmin.equals(currentUser.getEmail()) && !currentUser.isSuperAdmin())
            {
                logger.warn("The current user differs from the stated user.");
                return new ResponseEntity<>("The current user differs from the stated user", HttpStatus.BAD_REQUEST);
            }

            if(adminPasswordNew != null && adminPasswordNewRe != null && !adminPasswordNew.isEmpty() && !adminPasswordNewRe.isEmpty())
            {
                logger.debug(currentUser.getEmail() + " wants to change his password.");
                if(!adminPasswordNew.equals(adminPasswordNewRe))
                {
                    logger.warn("The stated passwords did not match");
                    return new ResponseEntity<>("The stated passwords did not match", HttpStatus.BAD_REQUEST);
                } else
                {
                    currentUser.setPassword(adminPasswordNew);
                    try
                    {
                        logger.debug("Saving new user password.");
                        userRepository.save(currentUser);
                    } catch (ConstraintViolationException e)
                    {
                        logger.warn("A database constraint was violated while saving the user.");
                        return new ResponseEntity<>("A database constraint was violated while saving the user.", HttpStatus.BAD_REQUEST);
                    }
                }
            }

            if(currentUser.isSuperAdmin())
            {
                logger.debug("The user is a super admin");
                try
                {
                    Resource resource = new ClassPathResource("myVerein.properties");
                    Properties properties = PropertiesLoaderUtils.loadProperties(resource);

                    logger.debug("Loaded settings file from classpath: " + properties.toString());

                    if (clubName != null && !clubName.isEmpty())
                    {
                        logger.debug("Setting club name to " + clubName);
                        properties.setProperty("clubName", clubName);
                    }

                    if (clubLogo != null && !clubLogo.isEmpty())
                    {
                        logger.debug("Saving club logo");
                        try
                        {
                            gridFSRepository.storeClubLogo(clubLogo);
                        } catch (MongoException e)
                        {
                            logger.warn("Problem while saving club logo: " + e.getMessage());
                            return new ResponseEntity<>("Problem while saving club logo: " + e.getMessage(), HttpStatus.BAD_REQUEST);
                        }
                    } else
                    {
                        System.err.println("Unable to see clubLogo");
                    }

                    if (databaseHost != null && !databaseHost.isEmpty())
                    {
                        logger.debug("Setting database host to " + databaseHost);
                        properties.setProperty("dbHost", databaseHost);
                    }

                    if (databasePort != null && !databasePort.isEmpty())
                    {
                        logger.debug("Setting database port to " + databasePort);
                        properties.setProperty("dbPort", databasePort);
                    }

                    if (databaseUser != null)
                    {
                        logger.debug("Setting database user to " + databaseUser);
                        properties.setProperty("dbUser", databaseUser);
                    }

                    if (databasePassword != null)
                    {
                        logger.debug("Setting database password");
                        properties.setProperty("dbPassword", databasePassword);
                    }

                    if (databaseCollection != null && !databaseCollection.isEmpty())
                    {
                        logger.debug("Setting database collection name " + databaseCollection);
                        properties.setProperty("dbName", databaseCollection);
                    }

                    if (rememberMeTokenKey != null && !rememberMeTokenKey.isEmpty())
                    {
                        logger.debug("Setting remember me token key " + rememberMeTokenKey);
                        properties.setProperty("rememberMeKey", rememberMeTokenKey);
                    }

                    logger.debug("Saving updated settings file.");
                    properties.store(new FileOutputStream(resource.getFile()), "Settings last changed by " + currentUser.getEmail() + " (" + LocalDateTime.now().toString() + ")");
                } catch (IOException e)
                {
                    logger.warn("Unable to update settings file: " + e.getMessage());
                    return new ResponseEntity<>("Unable to update settings file", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        } else
        {
            logger.warn("A user who is not an admin" + currentUser.getEmail() + " tries to change the settings ");
            return new ResponseEntity<>("You are not allowed to change these settings", HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>("Successfully updated settings", HttpStatus.OK);
    }
}
