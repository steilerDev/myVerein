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
import javax.xml.ws.Response;
import java.io.IOException;
import java.util.*;
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
        logger.trace("[{}] Starting to load settings", currentUser);
        Map<String, Object> settings;
        if(!currentUser.isAdmin())
        {
            logger.warn("[{}] The user is a non-admin and tries to access the settings", currentUser);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if(!currentUser.isSuperAdmin())
        {
            logger.info("[{}] The user is a non-superadmin and is accessing the settings", currentUser);
            settings = new HashMap<>();
            settings.put("administrationNotAllowedMessage", "You are not the super admin, and therefore you cannot adjust system settings.");
        } else
        {
            logger.debug("[{}] Loading settings for super admin", currentUser);

            settings = Settings.loadSettings(settingsRepository).getSettingsMap();

            if(gridFSRepository.findClubLogo() != null)
            {
                logger.debug("[{}] The club logo is available", currentUser);
                settings.put("clubLogoAvailable", true);
            }
        }

        settings.put("currentAdmin", currentUser.getSendingObjectOnlyEmailNameId());
        logger.info("[{}] Finished loading settings", currentUser);
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    /**
     * This function is saving the settings for the system durable. The function is invoked by POSTing the parameters to the URI /api/admin/settings.
     * @param currentAdmin If the logged in user is a super admin, this field specifies the new super admin. If the logged in user is a normal admin, this field needs to contain his email.
     * @param adminPasswordNew The new password for the currently logged in admin.
     * @param adminPasswordNewRe The retyped new password for the currently logged in admin.
     * @param clubName The club name.
     * @param clubLogo The club logo. If this parameter is present the former club logo is going to be replaced.
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
                                               @RequestParam Map<String, String> parameters,
                                               @RequestParam String currentAdminPassword,
                                               @CurrentUser User currentUser)
    {
        logger.trace("[{}] Starting to save settings", currentUser);
        Settings settings = Settings.loadSettings(settingsRepository);
        if (!passwordEncoder.isPasswordValid(currentUser.getPassword(), currentAdminPassword, currentUser.getSalt()))
        {
            logger.warn("[{}] The stated password is invalid", currentUser);
            return new ResponseEntity<>("The stated password is incorrect, please try again", HttpStatus.FORBIDDEN);
        } else if (!currentUser.isAdmin())
        {
            logger.warn("[{}] A user who is not an admin tries to change the settings", currentUser);
            return new ResponseEntity<>("You are not allowed to change these settings", HttpStatus.FORBIDDEN);
        } else
        {
            if (currentUser.isSuperAdmin())
            {
                ResponseEntity<String> systemSettingsChangeResponseEntity = changeSystemSettings(currentAdmin, clubName, clubLogo, parameters, settings, currentUser);
                if (systemSettingsChangeResponseEntity != null)
                {
                    return systemSettingsChangeResponseEntity;
                }
            } else
            {
                logger.debug("[{}] The user is only an admin", currentUser);
                if (currentAdmin != null && !currentAdmin.equals(currentUser.getEmail()))
                {
                    logger.warn("[{}] The current user differs from the stated user", currentUser);
                    return new ResponseEntity<>("The current user differs from the stated user", HttpStatus.BAD_REQUEST);
                }
            }

            if (adminPasswordNew != null && adminPasswordNewRe != null && !adminPasswordNew.isEmpty() && !adminPasswordNewRe.isEmpty())
            {
                logger.debug("[{}] Trying to change password", currentUser);
                ResponseEntity<String> passwordChangeResponseEntity = changePassword(adminPasswordNew, adminPasswordNewRe, currentUser);
                if (passwordChangeResponseEntity != null)
                {
                    return passwordChangeResponseEntity;
                }
            }
            logger.info("[{}] Successfully updated all settings", currentAdmin);
            return new ResponseEntity<>("Successfully updated settings", HttpStatus.OK);
        }
    }

    /**
     * This function changes the system settings according to the parameter. This function is only called if the user is a super admin.
     * @param currentAdmin If the logged in user is a super admin, this field specifies the new super admin. If the logged in user is a normal admin, this field needs to contain his email.
     * @param clubName The club name.
     * @param clubLogo The club logo. If this parameter is present the former club logo is going to be replaced.
     * @param parameters The complete map of all parameters, containing the custom user fields.
     * @param currentUser The currently logged in user.
     * @param settings The currently loaded settings object.
     * @return An HTTP response with a status code, if an error occurred an error message is bundled into the response, otherwise null.
     */
    private ResponseEntity<String> changeSystemSettings(String currentAdmin, String clubName, MultipartFile clubLogo, Map<String, String> parameters, Settings settings, User currentUser)
    {
        logger.debug("[{}] The user is a super admin", currentUser);
        // Changing super admin
        if(currentAdmin != null && !currentAdmin.equals(currentUser.getEmail()))
        {
            logger.warn("[{}] The super admin user is changing to {}", currentUser, currentAdmin);
            Division rootDivision = settings.getRootDivision();
            if(rootDivision == null)
            {
                logger.warn("[{}] Unable to find root division", currentUser);
                return new ResponseEntity<>("Unable to find root division", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            User newSuperAdmin = userRepository.findByEmail(currentAdmin);
            if(newSuperAdmin == null)
            {
                logger.warn("[{}] Unable to find new super admin {}", currentUser, currentAdmin);
                return new ResponseEntity<>("Unable to find new super admin", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            logger.debug("[{}] Saving new super admin {}", currentUser, newSuperAdmin);
            settings.getRootDivision().setAdminUser(newSuperAdmin);
            divisionRepository.save(rootDivision);
            logger.info("[{}] Successfully saved {} as new super admin", currentUser, currentAdmin);
        }
        // Change other settings
        if (clubName != null && !clubName.isEmpty())
        {
            logger.debug("[{}] Setting club name to {}", currentUser, clubName);
            Division rootDivision = settings.getRootDivision();
            if(rootDivision == null)
            {
                logger.warn("[{}] Unable to find root division", currentUser);
                return new ResponseEntity<>("Unable to find root division", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            //Changing and saving the root division
            rootDivision.setName(clubName);
            divisionRepository.save(rootDivision);
            settings.setClubName(clubName);
        }

        if (clubLogo != null && !clubLogo.isEmpty())
        {
            logger.debug("[{}] Saving club logo", currentUser);
            try
            {
                gridFSRepository.storeClubLogo(clubLogo);
            } catch (MongoException e)
            {
                logger.warn("[{}] Problem while saving club logo: {}", currentUser, e.getMessage());
                return new ResponseEntity<>("Problem while saving club logo: " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }

        settings.setCustomUserFields(updateCustomUserFields(parameters, currentUser, settings));

        logger.debug("[{}] Saving updated settings file", currentUser);
        settingsRepository.save(settings);
        logger.info("[{}] Successfully saved updated settings file", currentUser);
        return null;
    }

    /**
     * This function updates the defined custom user fields.
     * @param parameters A map containing the custom user fields (prefixed with 'cuf_'). If additionally a key-value pair starting with 'delete' or 'deleteContent is present, the field is going to be removed.
     * @param currentUser The currently logged in user.
     * @param settings The currently used settings object.
     * @return The new list of custom user fields.
     */
    private List<String> updateCustomUserFields(Map<String, String> parameters, User currentUser, Settings settings)
    {
        logger.debug("[{}] Gathering all custom user fields", currentUser);
        //Reducing parameters to custom user field parameters only and the value of the input
        List<String> reducedValues = parameters.keySet().parallelStream().filter(key -> key.startsWith("cuf_") && !parameters.get(key).trim().isEmpty()).distinct() //Only allowing distinct keys
                .map(key -> key.substring(4)) //Reducing the key to the initial 'name' value, used to create the fields by jQuery
                .collect(Collectors.toList());

        //Analysing the values: Checking if the value is deleted, renamed or created
        if (!reducedValues.isEmpty())
        {
            logger.info("[{}] No custom user fields specified, clearing fields");
            return null;
        } else
        {
            logger.debug("[{}] There are custom user fields available", currentUser);
            ArrayList<String> customUserFieldValues = new ArrayList<>();
            reducedValues.parallelStream().forEach(key -> {
                if (parameters.get("delete" + key) != null)
                {
                    logger.warn("[{}] Deleting custom user field {}", currentUser, key);
                    if (parameters.get("deleteContent" + key) != null)
                    {
                        logger.warn("[{}] Deleting content of custom user field {} on every user object", currentUser, key);
                        List<User> user = mongoTemplate.find(new Query(Criteria.where("customUserField." + key).exists(true)), User.class);
                        if (user != null && !user.isEmpty())
                        {
                            user.parallelStream().forEach(thisUser -> {
                                thisUser.removeCustomUserField(key);
                                logger.trace("[{}] Deleting custom user field content {} for user {}", currentUser, key, thisUser);
                                userRepository.save(thisUser);
                            });
                        }
                    }
                } else
                {
                    String value = parameters.get("cuf_" + key).trim();
                    if (!key.equals(value) && settings.getCustomUserFields().contains(key)) //The key was renamed
                    {
                        logger.debug("[{}] The custom user field {} changed to {}", currentUser, key, value);
                        List<User> user = mongoTemplate.find(new Query(Criteria.where("customUserField." + key).exists(true)), User.class);
                        if (user != null && !user.isEmpty())
                        {
                            user.parallelStream().forEach(thisUser -> {
                                thisUser.renameCustomUserField(key, value);
                                logger.trace("[{}] Renaming custom user field {} to {} for user {}", currentUser, key, value, thisUser);
                                userRepository.save(thisUser);
                            });
                        }
                    }
                    logger.debug("[{}] Adding {} as custom user field", currentUser, value);
                    customUserFieldValues.add(value);
                }
            });
            return customUserFieldValues;
        }
    }

    /**
     * This function changes the password of the currently logged in user.
     * @param newPassword The new password of the user.
     * @param newPasswordRe The retyped new password of the user.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code, if an error occurred an error message is bundled into the response, otherwise null.
     */
    private ResponseEntity<String> changePassword(String newPassword, String newPasswordRe, User currentUser)
    {
        logger.info("[{}] The user wants to change his password", currentUser);
        if (!newPassword.equals(newPasswordRe))
        {
            logger.warn("[{}] The stated passwords did not match", currentUser);
            return new ResponseEntity<>("The stated passwords did not match", HttpStatus.BAD_REQUEST);
        } else
        {
            currentUser.setPassword(newPassword);
            logger.debug("[{}] Saving new user password", currentUser);
            userRepository.save(currentUser);
            logger.info("[{}] Successfully saved new user password", currentUser);
            return null;
        }
    }

    /**
     * This function gathers all defined custom user fields. The function is invoked by GETting the URI /apic/admin/settings/customUserFields.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error error code is returned, otherwise a success code together with the list of all custom user fields is returned.
     */
    @RequestMapping(value = "customUserFields", produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<String>> getCustomUserFields(@CurrentUser User currentUser)
    {
        logger.trace("[{}] Gathering custom user fields", currentUser);
        List<String> customUserFields = Settings.loadSettings(settingsRepository).getCustomUserFields();
        if(customUserFields != null)
        {
            logger.debug("[{}] Returning custom user fields", currentUser);
            return new ResponseEntity<>(customUserFields, HttpStatus.OK);
        } else
        {
            logger.warn("[{}] Unable to gather custom user fields", currentUser);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
