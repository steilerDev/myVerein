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
package de.steilerdev.myVerein.server.controller.admin;

import com.mongodb.MongoException;
import de.steilerdev.myVerein.server.model.*;
import de.steilerdev.myVerein.server.security.CurrentUser;
import de.steilerdev.myVerein.server.security.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class handles all requests done through the settings page.
 */
@RestController
@RequestMapping("/api/admin/settings")
public class SettingsController
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GridFSRepository gridFSRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    private static Logger logger = LoggerFactory.getLogger(SettingsController.class);

    /**
     * This function is gathering the current settings of the application. The function is invoked by GETting the URI /api/admin/settings.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred a error code is returned, otherwise the map of all available settings is returned.
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Map<String, Object>> loadSettings(@CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Starting to load settings");
        Map<String, Object> settings;
        if(!currentUser.isAdmin())
        {
            logger.warn("[" + currentUser + "] The user is a non-admin and tries to access the settings");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if(!currentUser.isSuperAdmin())
        {
            logger.info("[" + currentUser + "] The user is a non-superadmin and is accessing the settings");
            settings = new HashMap<>();
            settings.put("administrationNotAllowedMessage", "You are not the super admin, and therefore you cannot adjust system settings.");
        } else
        {
            logger.debug("[" + currentUser + "] Loading settings for super admin");

            settings = Settings.loadSettings(settingsRepository).getSettingsMap();

            if(gridFSRepository.findClubLogo() != null)
            {
                logger.debug("[" + currentUser + "] The club logo is available");
                settings.put("clubLogoAvailable", true);
            }
        }

        settings.put("currentAdmin", currentUser.getSendingObjectOnlyEmailNameId());
        logger.info("[" + currentUser + "] Finished loading settings");
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    /**
     * This function is saving the settings for the system durable. In case the database connection changed, the system is restarted. The function is invoked by POSTing the parameters to the URI /api/admin/settings.
     * NOTE: At the moment the restarting of the application is not working correctly. To apply changed database settings the application needs to be redeployed manually from the management interface.
     * @param currentAdmin If the logged in user is a super admin, this field specifies the new super admin. If the logged in user is a normal admin, this field needs to contain his email.
     * @param adminPasswordNew The new password for the currently logged in admin.
     * @param adminPasswordNewRe The retyped new password for the currently logged in admin.
     * @param clubName The club name.
     * @param clubLogo The club logo. If this parameter is present the former club logo is going to be replaced.
     * @param databaseHost The hostname of the used MongoDB server.
     * @param databasePort The port of the used MongoDB server.
     * @param databaseUser The username, used to authenticate against the MongoDB server.
     * @param databasePassword The password, used to authenticate against the MongoDB server.
     * @param databaseCollection The name of the database collection, where the data of this system is stored in.
     * @param rememberMeTokenKey The phrase used to secure the remember me cookies.
     * @param parameters The complete map of all parameters, containing the custom user fields.
     * @param currentAdminPassword The password of the currently logged in user, used to authenticate the changes.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> saveSettings(@RequestParam(required = false) String currentAdmin,
                                               @RequestParam(required = false) String adminPasswordNew,
                                               @RequestParam(required = false) String adminPasswordNewRe,
                                               @RequestParam(required = false) String clubName,
                                               @RequestParam(required = false) MultipartFile clubLogo,
                                               @RequestParam(required = false) String databaseHost,
                                               @RequestParam(required = false) String databasePort,
                                               @RequestParam(required = false) String databaseUser,
                                               @RequestParam(required = false) String databasePassword,
                                               @RequestParam(required = false) String databaseCollection,
                                               @RequestParam Map<String, String> parameters,
                                               @RequestParam String currentAdminPassword,
                                               @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Starting to save settings");
        Settings settings = Settings.loadSettings(settingsRepository);
        if(!passwordEncoder.isPasswordValid(currentUser.getPassword(), currentAdminPassword, currentUser.getSalt()))
        {
            logger.warn("[" + currentUser + "] The stated password is invalid");
            return new ResponseEntity<>("The stated password is incorrect, please try again", HttpStatus.FORBIDDEN);
        } else if(currentUser.isAdmin())
        {
            if(currentUser.isSuperAdmin())
            {
                logger.debug("[" + currentUser + "] The user is a super admin");
                if(currentAdmin != null && !currentAdmin.equals(currentUser.getEmail()))
                {
                    logger.warn("[" + currentUser + "] The super admin user is changing to " + currentAdmin);
                    Division rootDivision = divisionRepository.findByName(settings.getClubName());
                    if(rootDivision == null)
                    {
                        logger.warn("[" + currentUser + "] Unable to find root division " + settings.getClubName());
                        return new ResponseEntity<>("Unable to find root division", HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    User newSuperAdmin = userRepository.findByEmail(currentAdmin);
                    if(newSuperAdmin == null)
                    {
                        logger.warn("[" + currentUser + "] Unable to find new super admin " + currentAdmin);
                        return new ResponseEntity<>("Unable to find new super admin", HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    logger.debug("[" + currentUser + "] Saving new super admin");
                    rootDivision.setAdminUser(newSuperAdmin);
                    divisionRepository.save(rootDivision);
                    logger.info("[" + currentUser + "] Successfully saved " + currentAdmin + " as new super admin");
                }
                try
                {
                    if (clubName != null && !clubName.isEmpty())
                    {
                        logger.debug("[" + currentUser + "] Setting club name to " + clubName);
                        Division rootDivision = divisionRepository.findByName(settings.getClubName());
                        if(rootDivision == null)
                        {
                            logger.warn("[" + currentUser + "] Unable to find former root division.");
                            return new ResponseEntity<>("Unable to find former root division", HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                        //Changing and saving the root division
                        rootDivision.setName(clubName);
                        divisionRepository.save(rootDivision);
                        settings.setClubName(clubName);
                    }

                    if (clubLogo != null && !clubLogo.isEmpty())
                    {
                        logger.debug("[" + currentUser + "] Saving club logo");
                        try
                        {
                            gridFSRepository.storeClubLogo(clubLogo);
                        } catch (MongoException e)
                        {
                            logger.warn("[" + currentUser + "] Problem while saving club logo: " + e.getMessage());
                            return new ResponseEntity<>("Problem while saving club logo: " + e.getMessage(), HttpStatus.BAD_REQUEST);
                        }
                    }

                    if (databaseHost != null && !databaseHost.isEmpty())
                    {
                        logger.debug("[" + currentUser + "] Setting database host to " + databaseHost);
                        settings.setDatabaseHost(databaseHost);
                    }

                    if (databasePort != null && !databasePort.isEmpty())
                    {
                        logger.debug("[" + currentUser + "] Setting database port to " + databasePort);
                        settings.setDatabasePort(databasePort);
                    }

                    if (databaseUser != null)
                    {
                        logger.debug("[" + currentUser + "] Setting database user to " + databaseUser);
                        settings.setDatabaseUser(databaseUser);
                    }

                    if (databasePassword != null)
                    {
                        logger.debug("[" + currentUser + "] Setting database password");
                        settings.setDatabasePassword(databasePassword);
                    }

                    if (databaseCollection != null && !databaseCollection.isEmpty())
                    {
                        logger.debug("[" + currentUser + "] Setting database collection name " + databaseCollection);
                        settings.setDatabaseName(databaseCollection);
                    }

                    logger.debug("[" + currentUser + "] Gathering all custom user fields");
                    //Reducing parameters to custom user field parameters only and the value of the input
                    List<String> reducedValues = parameters.keySet().parallelStream()
                            .filter(key -> key.startsWith("cuf_") && !parameters.get(key).trim().isEmpty())
                            .distinct() //Only allowing distinct keys
                            .map(key -> key.substring(4)) //Reducing the key to the initial 'name' value, used to create the fields by jQuery
                            .collect(Collectors.toList());

                    //Analysing the values and checking, if
                    if(!reducedValues.isEmpty())
                    {
                        logger.debug("[" + currentUser + "] There are custom user fields available");
                        ArrayList<String> customUserFieldValues = new ArrayList<>();
                        reducedValues.parallelStream().forEach(key -> {
                            if(parameters.get("delete" + key) != null)
                            {
                                logger.warn("[" + currentUser + "] Deleting custom user field " + key);
                                if(parameters.get("deleteContent" + key) != null)
                                {
                                    logger.warn("[" + currentUser + "] Deleting content of custom user field " + key + " on every user object");
                                    List<User> user = mongoTemplate.find(new Query(Criteria.where("customUserField." + key).exists(true)), User.class);
                                    if(user != null && !user.isEmpty())
                                    {
                                        user.parallelStream().forEach(thisUser -> {
                                            thisUser.removeCustomUserField(key);
                                            try
                                            {
                                                logger.trace("[" + currentUser + "] Deleting custom user field content " + key + " for user " + thisUser.getEmail());
                                                userRepository.save(thisUser);
                                            } catch (ConstraintViolationException e)
                                            {
                                                logger.warn("[" + currentUser + "] A database constraint was violated while trying to delete the custom user field " + key + " for user " + thisUser.getEmail() + ": " + e.getMessage());
                                            }
                                        });
                                    }
                                }
                            } else
                            {
                                String value = parameters.get("cuf_" + key).trim();
                                if(!key.equals(value) && settings.getCustomUserFields().contains(key)) //The key was renamed
                                {
                                    logger.debug("[" + currentUser + "] The custom user field " + key + " changed to " + value);
                                    List<User> user = mongoTemplate.find(new Query(Criteria.where("customUserField." + key).exists(true)), User.class);
                                    if(user != null && !user.isEmpty())
                                    {
                                        user.parallelStream().forEach(thisUser -> {
                                            thisUser.renameCustomUserField(key, value);
                                            try
                                            {
                                                logger.trace("[" + currentUser + "] Renaming custom user field " + key + " to " + value + " for user " + thisUser.getEmail());
                                                userRepository.save(thisUser);
                                            } catch (ConstraintViolationException e)
                                            {
                                                logger.warn("[" + currentUser + "] A database constraint was violated while trying to rename the custom user field " + key + " for user " + thisUser.getEmail() + ": " + e.getMessage());
                                            }
                                        });
                                    }
                                }
                                logger.debug("[" + currentUser + "] Adding " + value + " as custom user field");
                                customUserFieldValues.add(value);
                            }
                        });
                        settings.setCustomUserFields(customUserFieldValues);
                    }

                    logger.debug("[" + currentUser + "] Saving updated settings file");
                    settings.saveSettings(currentUser, settingsRepository);
                    logger.info("[" + currentUser + "] Successfully saved updated settings file");
                } catch (IOException e)
                {
                    logger.warn("[" + currentUser + "] Unable to update settings file: " + e.getMessage());
                    return new ResponseEntity<>("Unable to update settings file", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } else
            {
                logger.debug("[" + currentUser + "] The user is an admin");
                if(currentAdmin != null && !currentAdmin.equals(currentUser.getEmail()))
                {
                    logger.warn("[" + currentUser + "] The current user differs from the stated user");
                    return new ResponseEntity<>("The current user differs from the stated user", HttpStatus.BAD_REQUEST);
                }
            }

            if(adminPasswordNew != null && adminPasswordNewRe != null && !adminPasswordNew.isEmpty() && !adminPasswordNewRe.isEmpty())
            {
                logger.info("[" + currentUser + "] The user wants to change his password.");
                if(!adminPasswordNew.equals(adminPasswordNewRe))
                {
                    logger.warn("[" + currentUser + "] The stated passwords did not match");
                    return new ResponseEntity<>("The stated passwords did not match", HttpStatus.BAD_REQUEST);
                } else
                {
                    currentUser.setPassword(adminPasswordNew);
                    try
                    {
                        logger.debug("[" + currentUser + "] Saving new user password.");
                        userRepository.save(currentUser);
                        logger.info("[" + currentUser + "] Successfully saved new user password");
                    } catch (ConstraintViolationException e)
                    {
                        logger.warn("[" + currentUser + "] A database constraint was violated while saving the user: " + e.getMessage());
                        return new ResponseEntity<>("A database constraint was violated while saving the user.", HttpStatus.BAD_REQUEST);
                    }
                }
            }
        } else
        {
            logger.warn("[" + currentUser + "] A user who is not an admin tries to change the settings ");
            return new ResponseEntity<>("You are not allowed to change these settings", HttpStatus.FORBIDDEN);
        }
        logger.info("[" + currentUser + "] Successfully updated all settings");
        return new ResponseEntity<>("Successfully updated settings", HttpStatus.OK);
    }

    /**
     * This function gathers all defined custom user fields. The function is invoked by GETting the URI /apic/admin/settings/customUserFields.
     * @return An HTTP response with a status code. If an error occurred an error error code is returned, otherwise a success code together with the list of all custom user fields is returned.
     */
    @RequestMapping(value = "customUserFields", produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getCustomUserFields(@CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Gathering custom user fields");
        List<String> customUserFields = Settings.loadSettings(settingsRepository).getCustomUserFields();
        if(customUserFields != null)
        {
            logger.debug("[" + currentUser + "] Returning custom user fields");
            return new ResponseEntity<>(customUserFields, HttpStatus.OK);
        } else
        {
            logger.warn("[" + currentUser + "] Unable to gather custom user fields");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
