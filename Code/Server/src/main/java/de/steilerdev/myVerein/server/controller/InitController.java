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

import com.mongodb.*;
import de.steilerdev.myVerein.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

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

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    private ServerAddress mongoAddress = null;
    private List<MongoCredential> mongoCredential = null;
    private String databaseName = null;

    private boolean succesfullyStoredDataInDatabase = false;
    private boolean successfullyReloadedApplicationContext = false;

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
            int databasePortInt = 27017;
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
                    databasePortInt = Integer.parseInt(databasePort);
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

            if(!mongoIsAvailable(databaseHost, databasePortInt, databaseUser, databasePassword, databaseCollection))
            {
                logger.warn("The stated MongoDB is not available");
                return new ResponseEntity<>(messageSource.getMessage("init.message.settings.mongoNotAvailable", null, "The stated MongoDB is not available", locale), HttpStatus.BAD_REQUEST);
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
                databaseName = databaseCollection;
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

            try
            {
                //Un-setting init flag.
                settingsRepository.setInitSetup(false);
            } catch (IOException e)
            {
                logger.warn("Unable to save settings.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.savingSettingsError", null, "Unable to save settings, please try again", locale), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            logger.debug("Everything in place, now the settings are stored durable.");

            if(!savingUserAndDivision(superAdmin, rootDivision))
            {
                logger.warn("Storing data into database was not successfully.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.savingAdminError", null, "Unable to save new super admin, please try again", locale), HttpStatus.INTERNAL_SERVER_ERROR);
            } else if(!savingSettings(superAdmin))
            {
                logger.warn("Unable to save settings or refresh application context");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.savingSettingsError", null, "Unable to save settings, please try again", locale), HttpStatus.INTERNAL_SERVER_ERROR);
            } else
            {
                logger.debug("Successfully saved new admin, settings and restarted application context.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.success", null, "Successfully saved the new super admin, updated all settings and restarted the application. Refresh this page and start using myVerein.", locale) , HttpStatus.OK);
            }
        }
    }

    private boolean mongoIsAvailable(String databaseHost, int databasePort, String databaseUser, String databasePassword, String databaseCollection)
    {
        logger.debug("Creating mongo client to test connection");

        //Creating credentials
        if(!databaseUser.isEmpty() && !databasePassword.isEmpty())
        {
            mongoCredential = Arrays.asList(MongoCredential.createMongoCRCredential(databaseUser, databaseCollection, databasePassword.toCharArray()));
        }

        //Creating server address
        try
        {
            mongoAddress = new ServerAddress(databaseHost, databasePort);
        } catch (UnknownHostException e)
        {
            logger.warn("Unable to resolve mongoDB host: " + e.getMessage());
            return false;
        }

        //Creating and testing mongo client
        MongoClient mongoClient = new MongoClient(mongoAddress, mongoCredential);
        try
        {
            //Checking if connection REALLY works
            List<String> databases = mongoClient.getDatabaseNames();
            if(databases == null)
            {
                logger.warn("The list of databases is null");
                return false;
            } else if(databases.isEmpty())
            {
                logger.info("The databases are empty");
                return true;
            } else
            {
                logger.debug("The database connection seems okay.");
                return true;
            }
        } catch (MongoException e)
        {
            logger.warn("Unable to receive list of present databases: " + e.getMessage());
            return false;
        } finally
        {
            if(mongoClient != null)
            {
                mongoClient.close();
            }
        }
    }

    private boolean savingUserAndDivision(User user, Division division)
    {
        if(mongoAddress != null && databaseName != null && !databaseName.isEmpty())
        {
            MongoClient mongoClient = new MongoClient(mongoAddress, mongoCredential);

            logger.debug("Saving user using new MongoDB connection");
            DB mongoDB = mongoClient.getDB(databaseName);
            if (mongoClient.getDatabaseNames().contains(databaseName))
            {
                logger.warn("The database already contains the defined collection, dropping content.");
                mongoDB.dropDatabase();
            }

            try
            {
                logger.debug("Storing user");
                DBObject userObject = (DBObject) mappingMongoConverter.convertToMongoType(user);
                userObject.put("_class", user.getClass().getCanonicalName());
                mongoDB.getCollection(user.getClass().getSimpleName().toLowerCase()).insert(userObject);

                logger.debug("Storing division");
                DBObject divisionObject = (DBObject) mappingMongoConverter.convertToMongoType(division);
                divisionObject.put("_class", division.getClass().getCanonicalName());
                mongoDB.getCollection(division.getClass().getSimpleName().toLowerCase()).insert(divisionObject);

                return true;
            } catch (MongoException e)
            {
                logger.warn("Unable to store object in database");
                return false;
            } finally
            {
                if(mongoClient != null)
                {
                    mongoClient.close();
                }
            }
        } else
        {
            logger.warn("Unable to save user, because the new MongoDB connection is not available");
            return false;
        }
    }

    private boolean savingSettings(User currentUser)
    {
        try
        {
            //This call is restarting the application.
            settingsRepository.saveSettings(currentUser);
            return true;
        } catch (BeansException | IllegalStateException | MongoException e)
        {
            logger.warn("Unable to refresh application context: " + e.getMessage());
            return false;
        } catch (IOException e)
        {
            logger.warn("Unable to save settings");
            return false;
        }
    }
}
