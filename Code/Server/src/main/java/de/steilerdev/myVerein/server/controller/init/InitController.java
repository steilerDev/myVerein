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
package de.steilerdev.myVerein.server.controller.init;

import com.mongodb.*;
import de.steilerdev.myVerein.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/init")
public class InitController
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReloadableResourceBundleMessageSource messageSource;

    @Autowired
    private MappingMongoConverter mappingMongoConverter;

    private ServerAddress mongoAddress = null;
    private List<MongoCredential> mongoCredential = null;
    private String databaseName = null;

    private static Logger logger = LoggerFactory.getLogger(InitController.class);

    /**
     * This function is temporarily saving the general settings during the initial setup. The values are stored permanently after calling the initSuperAdmin function. This function is invoked by POSTing the parameters to the URI /api/init/settings.
     * @param clubName The name of the club.
     * @param databaseHost The hostname of the MongoDB server (Default localhost).
     * @param databasePort The port of the MongoDB server (Default 27017).
     * @param databaseUser The user used to authenticate against the MongoDB server (may be empty if not needed).
     * @param databasePassword The password used to authenticate against the MongoDB server (may be empty if not needed).
     * @param databaseCollection The name of the database collection (Default myVerein).
     * @param locale The current locale of the user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(value = "settings", method = RequestMethod.POST)
    public ResponseEntity<String> initSettings(@RequestParam String clubName,
                                               @RequestParam String databaseHost,
                                               @RequestParam String databasePort,
                                               @RequestParam String databaseUser,
                                               @RequestParam String databasePassword,
                                               @RequestParam String databaseCollection,
                                               Locale locale)
    {
        logger.trace("Starting initial settings configuration");
        Settings settings = new Settings();
        if(!settings.isInitialSetup())
        {
            logger.warn("An initial setup API was used, even though the system is already configured.");
            return new ResponseEntity<>(messageSource.getMessage("init.message.settings.notAllowed", null, "You are not allowed to perform this action at the moment", locale), HttpStatus.BAD_REQUEST);
        } else if(clubName.isEmpty())
        {
            logger.warn("The club name is not present");
            return new ResponseEntity<>(messageSource.getMessage("init.message.settings.noName", null, "The club name is required", locale), HttpStatus.BAD_REQUEST);
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
                    logger.warn("The database port does not seem to be a number " + databasePort + ", " + e.getMessage());
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

//            try
//            {
                logger.debug("Temporarily storing settings information");
                settings.setClubName(clubName);
//                settings.setDatabaseHost(databaseHost);
//                settings.setDatabasePort(databasePort);
//                settings.setDatabaseUser(databaseUser);
//                settings.setDatabasePassword(databasePassword);
//                settings.setDatabaseName(databaseCollection);
                databaseName = databaseCollection;
                savingInitialSetup(null, null, settings);
//            } catch (IOException e)
//            {
//                logger.warn("Unable to save settings.");
//                return new ResponseEntity<>(messageSource.getMessage("init.message.settings.savingSettingsError", null, "Unable to save settings, please try again", locale), HttpStatus.INTERNAL_SERVER_ERROR);
//            }
            logger.info("Successfully stored and validated settings information");
            return new ResponseEntity<>(messageSource.getMessage("init.message.settings.savingSettingsSuccess", null, "Successfully saved settings", locale), HttpStatus.OK);
        }
    }

    /**
     * This function is creating the initial super admin, as well as the root division. On top of that it stores all information durable, as well as restarts the application. The function is invoked by POSTing the parameters to the URI /api/init/superAdmin.
     * NOTE: At the moment the restarting of the application is not working correctly. To apply changed database settings the application needs to be redeployed manually from the management interface.
     * @param firstName The first name of the new super admin.
     * @param lastName The last name of the new super admin.
     * @param email The email of the new super admin.
     * @param password The password of the new super admin.
     * @param passwordRe The retyped password of the new super admin.
     * @param locale The current locale of the user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(value = "superAdmin", method = RequestMethod.POST)
    public ResponseEntity<String> initSuperAdmin(@RequestParam String firstName,
                                                 @RequestParam String lastName,
                                                 @RequestParam String email,
                                                 @RequestParam String password,
                                                 @RequestParam String passwordRe,
                                                 Locale locale)
    {
        Settings settings = new Settings();
        logger.trace("Starting initial admin configuration");
        if(!settings.isInitialSetup())
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
            Division rootDivision = new Division(settings.getClubName(), null, superAdmin, null);

            settings.setInitialSetup(false);

            logger.info("Everything in place, now the settings are stored durable.");

            if(!savingInitialSetup(superAdmin, rootDivision, null))
            {
                logger.warn("Storing data into database was not successfully.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.savingAdminError", null, "Unable to save new super admin, please try again", locale), HttpStatus.INTERNAL_SERVER_ERROR);
            } else
            {
                logger.info("Successfully saved new admin, settings and restarted application context.");
                return new ResponseEntity<>(messageSource.getMessage("init.message.admin.success", null, "Successfully saved the new super admin, updated all settings and restarted the application. Refresh this page and start using myVerein.", locale) , HttpStatus.OK);
            }
        }
    }

    /**
     * This function is validating the provided information of the MongoDB server, by establishing a test connection.
     * @param databaseHost The hostname of the MongoDB server.
     * @param databasePort The port of the MongoDB server.
     * @param databaseUser The user used to authenticate against the MongoDB server (may be empty if not needed).
     * @param databasePassword The password used to authenticate against the MongoDB server (may be empty if not needed).
     * @param databaseCollection The name of the database collection.
     * @return True if the connection was successfully established, false otherwise.
     */
    private boolean mongoIsAvailable(String databaseHost, int databasePort, String databaseUser, String databasePassword, String databaseCollection)
    {
        logger.trace("Testing MongoDB connection");

        if(!databaseUser.isEmpty() && !databasePassword.isEmpty())
        {
            logger.debug("Credentials have been provided");
            mongoCredential = Arrays.asList(MongoCredential.createMongoCRCredential(databaseUser, databaseCollection, databasePassword.toCharArray()));
        }

        try
        {
            logger.debug("Creating server address");
            mongoAddress = new ServerAddress(databaseHost, databasePort);
        } catch (UnknownHostException e)
        {
            logger.warn("Unable to resolve server host: " + e.getMessage());
            return false;
        }

        logger.debug("Creating mongo client");
        MongoClient mongoClient = new MongoClient(mongoAddress, mongoCredential);
        try
        {
            //Checking if connection REALLY works
            logger.debug("Establishing connection now.");
            List<String> databases = mongoClient.getDatabaseNames();
            if(databases == null)
            {
                logger.warn("The returned list of databases is null");
                return false;
            } else if(databases.isEmpty())
            {
                logger.info("The databases are empty");
                return true;
            } else
            {
                logger.debug("The database connection seems okay");
                return true;
            }
        } catch (MongoException e)
        {
            logger.warn("Unable to receive list of present databases: " + e.getMessage());
            return false;
        } finally
        {
            logger.debug("Closing mongo client");
            mongoClient.close();
        }
    }

    /**
     * This function is establishing a connection to the new database and storing the new super user as well as the new root division. To correctly execute this function the {@link #mongoIsAvailable} function should be called first.
     * @param user The new super admin.
     * @param division The new root division.
     * @return True if the operation was successfully, false otherwise.
     */
    private boolean savingInitialSetup(User user, Division division, Settings settings)
    {
        if(mongoAddress != null && databaseName != null && !databaseName.isEmpty())
        {
            logger.trace("Saving user and division using new MongoDB connection");

            MongoClient mongoClient = new MongoClient(mongoAddress, mongoCredential);

            DB mongoDB = mongoClient.getDB(databaseName);
            if (mongoClient.getDatabaseNames().contains(databaseName))
            {
                logger.warn("The database already contains the defined collection, dropping content.");
                mongoDB.dropDatabase();
            }

            try
            {
                if(user != null)
                {
                    logger.debug("Storing user");
                    DBObject userObject = (DBObject) mappingMongoConverter.convertToMongoType(user);
                    userObject.put("_class", user.getClass().getCanonicalName());
                    mongoDB.getCollection(user.getClass().getSimpleName().toLowerCase()).insert(userObject);
                }

                if(division != null)
                {
                    logger.debug("Storing division");
                    DBObject divisionObject = (DBObject) mappingMongoConverter.convertToMongoType(division);
                    divisionObject.put("_class", division.getClass().getCanonicalName());
                    mongoDB.getCollection(division.getClass().getSimpleName().toLowerCase()).insert(divisionObject);
                }

                if(settings != null)
                {
                    logger.debug("Storing settings");
                    settingsRepository.save(settings);
                    DBObject settingsObject = (DBObject) mappingMongoConverter.convertToMongoType(settings);
                    settingsObject.put("_class", settings.getClass().getCanonicalName());
                    mongoDB.getCollection(settings.getClass().getSimpleName().toLowerCase()).insert(settingsObject);
                }

                logger.info("Successfully saved root division and super admin");
                return true;
            } catch (MongoException e)
            {
                logger.warn("Unable to store root division and super admin in database");
                return false;
            } finally
            {
                mongoClient.close();
            }
        } else
        {
            logger.warn("Unable to save super admin and new root division, because the new MongoDB connection is not available");
            return false;
        }
    }

    /**
     * This function is clearing the current database and creates a new set of test data. This function is called by POSTing the URI '/'
     * @return In general it should just return okay. No error checking is done!
     */
    @RequestMapping(value = "database", method = RequestMethod.POST)
    public ResponseEntity<String> createDatabaseExample()
    {
        logger.debug("Deleting old database and reloading database example.");
        resetDatabase();

        User user1 = new User("Frank", "Steiler", "frank@steiler.eu", "asdf");
        user1.setBirthday(LocalDate.of(1994, 6, 28));
        user1.setActiveSince(LocalDate.of(2000, 1, 1));
        user1.setIban("DE46500700100927353010");
        user1.setBic("BYLADEM1001");
        user1.setCity("Stuttgart");
        user1.setZipCode("70190");
        user1.setStreetNumber("27");
        user1.setStreet("Metzstraße");
        user1.setCountry("Germany");
        user1.setGender(User.Gender.MALE);
        User user2 = new User("John", "Doe", "john@doe.com", "asdf");
        user2.setActiveSince(LocalDate.of(1999, 1, 1));
        user2.setPassiveSince(LocalDate.of(2000, 6, 1));
        User user3 = new User("Peter", "Enis", "peter@enis.com", "asdf");
        User user4 = new User("Luke", "Skywalker", "luke@skywalker.com", "asdf");
        user4.setGender(User.Gender.MALE);
        User user5 = new User("Marty", "McFly", "marty@mcfly.com", "asdf");
        User user6 = new User("Tammo", "Schwindt", "tammo@tammon.de", "asdf");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);
        userRepository.save(user5);
        userRepository.save(user6);

        Division div1 = new Division("myVerein", null, user1, null);
        Division div2 = new Division("Rugby", null, user2, div1);
        Division div3 = new Division("Soccer", null, null, div1);
        Division div4 = new Division("Rugby - 1st team", null, user2, div2);
        Division div5 = new Division("Rugby - 2nd team", null, user3, div2);

        divisionRepository.save(div1);
        divisionRepository.save(div2);
        divisionRepository.save(div3);
        divisionRepository.save(div4);
        divisionRepository.save(div5);

        int thisMonth = LocalDateTime.now().getMonthValue();
        int thisYear = LocalDateTime.now().getYear();

        Event event1 = new Event();
        event1.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 20, 13, 0));
        event1.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 20, 14, 0));
        event1.setName("Super Event 1");
        event1.setLocation("DHBW Stuttgart");
        event1.setLocationLat(48.7735272);
        event1.setLocationLng(9.171102399999995);
        event1.setDescription("Super event at awesome location with great people");
        event1.addDivision(div2);
        event1.setEventAdmin(user1);
        event1.updateMultiDate();
        event1.setLastChanged(LocalDateTime.now());

        Event event2 = new Event();
        event2.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 20, 13, 0));
        event2.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 21, 13, 0));
        event2.setName("Super Event 2");
        event2.addDivision(div3);
        event2.setEventAdmin(user4);
        event2.updateMultiDate();
        event2.setLastChanged(LocalDateTime.now());

        Event event3 = new Event();
        event3.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 21, 13, 0));
        event3.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 21, 13, 5));
        event3.setName("Super Event 3");
        event3.addDivision(div1);
        event3.setEventAdmin(user1);
        event3.updateMultiDate();
        event3.setLastChanged(LocalDateTime.now());

        Event event4 = new Event();
        event4.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 11, 13, 0));
        event4.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 15, 13, 5));
        event4.setName("Super Event 4");
        event4.addDivision(div1);
        event4.setEventAdmin(user2);
        event4.updateMultiDate();
        event4.setLastChanged(LocalDateTime.now());

        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
        eventRepository.save(event4);

        user1.replaceDivisions(divisionRepository, eventRepository, div1);
        user2.replaceDivisions(divisionRepository, eventRepository, div2, div4);
        user3.replaceDivisions(divisionRepository, eventRepository, div2);
        user4.replaceDivisions(divisionRepository, eventRepository, div3);
        user5.replaceDivisions(divisionRepository, eventRepository, div2, div4);

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);
        userRepository.save(user5);

        Message message1 = new Message("Hello world", user1, divisionRepository.findById(div1.getId()));
        Message message2 = new Message("Hello world, too", user2, divisionRepository.findById(div1.getId()));
        messageRepository.save(message1);
        messageRepository.save(message2);

        Settings systemSettings = new Settings();
        systemSettings.setClubName("myVerein");
        List<String> customUserFields = new ArrayList<>();
        customUserFields.add("Membership number");
        systemSettings.setCustomUserFields(customUserFields);
        systemSettings.setRootDivision(div1);
        settingsRepository.save(systemSettings);

        return new ResponseEntity<>("Successfully reset database to example", HttpStatus.OK);
    }

    /**
     * This function is resetting the complete database.
     */
    private void resetDatabase()
    {
        new Thread(() -> {
            try
            {
                logger.debug("Trying to drop the current collection.");
                mongoTemplate.getDb().dropDatabase();
                logger.debug("Successfully dropped current collection");
            } catch (MongoTimeoutException e)
            {
                logger.debug("Unable to drop database, because the database is not available.");
            }}).run();
    }
}
