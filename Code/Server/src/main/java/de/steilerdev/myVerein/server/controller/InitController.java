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

import de.steilerdev.myVerein.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Locale;

@Controller
@RequestMapping("/init")
public class InitController
{
    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private ReloadableResourceBundleMessageSource messageSource;

    private static Logger logger = LoggerFactory.getLogger(InitController.class);

    @RequestMapping(value = "settings")
    public ResponseEntity<String> initSettings(@RequestParam String clubName,
                                               @RequestParam String databaseHost,
                                               @RequestParam String databasePort,
                                               @RequestParam String databaseUser,
                                               @RequestParam String databasePassword,
                                               @RequestParam String databaseCollection,
                                               @RequestParam String rememberMeTokenKey,
                                               Locale locale)
    {
        logger.debug("Starting initial configuration");
        if(!settingsRepository.isInitSetup())
        {
            logger.warn("An initial setup API was used, even though the system is already configured.");
            return new ResponseEntity<>(messageSource.getMessage("init.message.settings.notAllowed", null, "You are not allowed to perform this action at the moment", locale), HttpStatus.BAD_REQUEST);
        } else if(clubName.isEmpty() || rememberMeTokenKey.isEmpty())
        {
            logger.warn("The club name or remember me key is not present");
            return new ResponseEntity<>(messageSource.getMessage("init.message.settings.noKeyOrName", null, "The club name and remember me key is required", locale), HttpStatus.BAD_REQUEST);
        } else
        {
            if(databaseHost.isEmpty())
            {
                logger.warn("The database host is empty, using default value.");
                databaseHost = "localhost";
            }
            if(databasePort.isEmpty())
            {
                logger.warn("The database port is empty, using default value.");
                databasePort = "27017";
            } else
            {
                try
                {
                    Integer.parseInt(databasePort);
                } catch (NumberFormatException e)
                {
                    logger.warn("The database port seems not to be a number " + databasePort);
                    return new ResponseEntity<>(messageSource.getMessage("init.message.settings.dbPortNoNumber", null, "The database port needs to be a number", locale), HttpStatus.BAD_REQUEST);
                }
            }
            if(databaseCollection.isEmpty())
            {
                logger.warn("The database collection name is empty, using default value");
                databaseCollection = "myVerein";
            }

            try
            {
                logger.debug("Temporarily storing information");
                settingsRepository.setClubName(clubName);
                settingsRepository.setDatabaseHost(databaseHost);
                settingsRepository.setDatabasePort(databasePort);
                settingsRepository.setDatabaseUser(databaseUser);
                settingsRepository.setDatabasePassword(databasePassword);
                settingsRepository.setDatabaseName(databaseCollection);
                settingsRepository.setRememberMeKey(rememberMeTokenKey);
            } catch (IOException e)
            {
                logger.warn("Unable to save settings.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.settings.savingSettingsError", null, "Unable to save settings, please try again", locale), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(messageSource.getMessage("init.message.settings.savingSettingsSuccess", null, "Successfully saved settings", locale), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "superAdmin")
    public ResponseEntity<String> initSuperAdmin(@RequestParam String firstName,
                                                 @RequestParam String lastName,
                                                 @RequestParam String email,
                                                 @RequestParam String password,
                                                 @RequestParam String passwordRe,
                                                 Locale locale)
    {
        if(!settingsRepository.isInitSetup())
        {
            logger.warn("An initial setup API was used, even though the system is already configured.");
            return new ResponseEntity<>(messageSource.getMessage("init.message.admin.notAllowed", null, "You are not allowed to perform this action at the moment", locale), HttpStatus.BAD_REQUEST);
        } else if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || passwordRe.isEmpty())
        {
            logger.warn("A required parameter of the super admin is empty or missing during initial configuration");
            return new ResponseEntity<>(messageSource.getMessage("init.message.admin.missingParameter", null, "A required parameter is empty or missing", locale), HttpStatus.BAD_REQUEST);
        } else if(!password.equals(passwordRe))
        {
            logger.warn("The password and the re-typed password do not match!");
            return new ResponseEntity<>(messageSource.getMessage("init.message.admin.passwordMatchError", null, "The password and the re-typed password do not match", locale), HttpStatus.BAD_REQUEST);
        } else
        {
            logger.debug("Creating a new initial user.");
            User superAdmin = new User();
            superAdmin.setFirstName(firstName);
            superAdmin.setLastName(lastName);
            superAdmin.setEmail(email);
            superAdmin.setPassword(password);

            logger.debug("Creating a new initial root division.");
            Division rootDivision = new Division(settingsRepository.getClubName(), null, superAdmin, null);

            //Saving changes and restarting context
            try
            {
                settingsRepository.setInitSetup(false);
                //This call is restarting the application.
                settingsRepository.saveSettings(superAdmin);
            } catch (IOException e)
            {
                logger.warn("Unable to save settings.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.savingSettingsError", null, "Unable to save settings, please try again", locale), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            try
            {
                userRepository.save(superAdmin);
                divisionRepository.save(rootDivision);
            } catch (ConstraintViolationException e)
            {
                logger.warn("A database constraint was violated while saving the new super admin.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.constraintViolation", null, "A database constraint was violated while saving the new super admin", locale), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(messageSource.getMessage("init.message.admin.savingAdminSuccess", null, "Successfully saved the new super admin. The settings are now being saved and the application is restarted.", locale) , HttpStatus.OK);
        }
    }
}
