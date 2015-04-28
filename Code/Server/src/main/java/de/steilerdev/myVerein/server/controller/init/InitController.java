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
import de.steilerdev.myVerein.server.model.division.Division;
import de.steilerdev.myVerein.server.model.division.DivisionRepository;
import de.steilerdev.myVerein.server.model.event.Event;
import de.steilerdev.myVerein.server.model.event.EventRepository;
import de.steilerdev.myVerein.server.model.message.Message;
import de.steilerdev.myVerein.server.model.message.MessageRepository;
import de.steilerdev.myVerein.server.model.settings.Settings;
import de.steilerdev.myVerein.server.model.settings.SettingsHelper;
import de.steilerdev.myVerein.server.model.settings.SettingsRepository;
import de.steilerdev.myVerein.server.model.user.Gender;
import de.steilerdev.myVerein.server.model.user.User;
import de.steilerdev.myVerein.server.model.user.UserRepository;
import de.steilerdev.myVerein.server.security.CurrentUser;
import de.steilerdev.myVerein.server.security.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.net.UnknownHostException;
import java.security.Security;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is managing the initial setup of the system
 */
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

    private final Logger logger = LoggerFactory.getLogger(InitController.class);

    /**
     * This function is creating the initial settings for this system. It is creating an initial administrating user, as well as an initial division root.
     * This function can only be executed if either the initial setup up flag is set in the database or if there is no settings document available. The function is resetting the specified collection.
     * To change the database settings, the 'myVerein.properties' file has to be adjusted and the server needs to be restarted.
     * @param clubName The new name of the club.
     * @param firstName The first name of the administrating user.
     * @param lastName The last name of the administrating user.
     * @param email The email of the administrating user.
     * @param password The new password of the administrating user.
     * @param passwordRe The retyped password of the administrating user.
     * @param createExample If this parameter is set to 'on' the function is going to populate the system with example data.
     * @param request The user's request.
     * @param locale The currently set locale of the user.
     * @return A response enitity containing either a success or a failure code, together with a message.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> initSettings(@RequestParam String clubName,
                                               @RequestParam String firstName,
                                               @RequestParam String lastName,
                                               @RequestParam String email,
                                               @RequestParam String password,
                                               @RequestParam String passwordRe,
                                               @RequestParam(required = false) String createExample,
                                               HttpServletRequest request,
                                               Locale locale)
    {
        final String clientIpAddr = SecurityHelper.getClientIpAddr(request);
        logger.trace("[{}] Starting initial configuration", clientIpAddr);
        if(!SettingsHelper.initialSetupNeeded(settingsRepository))
        {
            logger.warn("[{}] An initial setup API was used, even though the system is already configured", clientIpAddr);
            return new ResponseEntity<>("An initial setup API was used, even though the system is already configured", HttpStatus.FORBIDDEN);
        } else if(clubName.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || passwordRe.isEmpty())
        {
            logger.warn("[{}] A required parameter is missing during the initial setup", clientIpAddr);
            return new ResponseEntity<>("A required parameter is missing", HttpStatus.BAD_REQUEST);
        } else if(!password.equals(passwordRe))
        {
            logger.warn("[{}] The provided administrator passwords do not match", clientIpAddr);
            return new ResponseEntity<>("The provided administrator passwords do not match", HttpStatus.BAD_REQUEST);
        } else
        {

            logger.debug("[{}] Resetting the database", clientIpAddr);
            resetDatabase();

            User admin = new User(firstName, lastName, email, password);
            Division rootDivision = new Division(clubName, null, admin, null);
            // Initial saving needed, to create id of the user.
            userRepository.save(admin);
            divisionRepository.save(rootDivision);

            // Assigning admin user to root division
            admin.replaceDivisions(divisionRepository, eventRepository, rootDivision);
            userRepository.save(admin);
            divisionRepository.save(rootDivision);

            // Saving settings
            Settings settings = new Settings();
            settings.setInitialSetup(false);
            settings.setClubName(clubName);
            settings.setRootDivision(rootDivision);
            settingsRepository.save(settings);

            if(createExample != null && !createExample.isEmpty() && createExample.equals("on"))
            {
                logger.info("[{}] Creating example data", clientIpAddr);
                createExampleData(rootDivision, admin);
            }

            logger.info("[{}] Successfully stored and validated settings", clientIpAddr);
            return new ResponseEntity<>("Successfully saved settings", HttpStatus.OK);
        }
    }

    /*

        WARNING: The following function are lazily hacked together!

     */


    /**
     * This function is clearing the current database and creates a new set of test data. This function is called by POSTing the URI '/api/init'. This function should be removed in a production environment.
     * @return In general it should just return okay. No error checking is done!
     */
    @RequestMapping(value = "database", method = RequestMethod.POST)
    public ResponseEntity<String> createSystemExample()
    {
        logger.debug("Deleting old database and reloading database example.");
        resetDatabase();

        User admin = new User("Frank", "Steiler", "frank@steiler.eu", "asdf");
        admin.setBirthday(LocalDate.of(1994, 6, 28));
        admin.setActiveSince(LocalDate.of(2000, 1, 1));
        admin.setIban("DE46500700100927353010");
        admin.setBic("BYLADEM1001");
        admin.setCity("Stuttgart");
        admin.setZipCode("70190");
        admin.setStreetNumber("27");
        admin.setStreet("Metzstraße");
        admin.setCountry("Germany");
        admin.setGender(Gender.MALE);

        userRepository.save(admin);

        Division rootDivision = new Division("myVerein", null, admin, null);
        divisionRepository.save(rootDivision);

        Settings systemSettings = new Settings();
        systemSettings.setClubName("myVerein");
        List<String> customUserFields = new ArrayList<>();
        customUserFields.add("Membership number");
        systemSettings.setCustomUserFields(customUserFields);
        systemSettings.setRootDivision(rootDivision);
        settingsRepository.save(systemSettings);

        createExampleData(rootDivision, admin);

        return new ResponseEntity<>("Successfully reset database to example", HttpStatus.OK);
    }

    /**
     * This function creates a set of example data, including example user {@link #createExampleUser(User)}, example division {@link #createExampleDivision(List, Division)}, example events {@link #createExampleEvents(List, List)} and example messages
     * @param rootDivision The current root division of the system (This division is not altered).
     * @param adminUser The current admin user of the system (This user is not altered).
     */
    private void createExampleData(Division rootDivision, User adminUser)
    {
        List<User> user = createExampleUser(adminUser);
        List<Division> divisions = createExampleDivision(user, rootDivision);

        createExampleEvents(user, divisions);
        subscribeUserToDivision(user, divisions);
        createExampleMessages(divisions);
    }

    /**
     * This function creates a set of user and stores them permanently.
     * @param adminUser The admin user. Every user is checked to not match the email from the allready created admin user.
     * @return The list of created user.
     */
    private List<User> createExampleUser(User adminUser)
    {
        ArrayList<User> users = new ArrayList<>();

        User testUser = new User("John", "Doe", "john@doe.com", "asdf");
        testUser.setActiveSince(LocalDate.of(1999, 1, 1));
        testUser.setPassiveSince(LocalDate.of(2000, 6, 1));

        if(!testUser.getEmail().equals(adminUser.getEmail()))
        {
            users.add(testUser);
        }

        testUser = new User("Peter", "Enis", "peter@enis.com", "asdf");
        if(!testUser.getEmail().equals(adminUser.getEmail()))
        {
            users.add(testUser);
        }

        testUser = new User("Luke", "Skywalker", "luke@skywalker.com", "asdf");
        testUser.setGender(Gender.MALE);
        if(!testUser.getEmail().equals(adminUser.getEmail()))
        {
            users.add(testUser);
        }

        testUser = new User("Marty", "McFly", "marty@mcfly.com", "asdf");
        if(!testUser.getEmail().equals(adminUser.getEmail()))
        {
            users.add(testUser);
        }

        testUser = new User("Tammo", "Schwindt", "tammo@tammon.de", "asdf");
        if(!testUser.getEmail().equals(adminUser.getEmail()))
        {
            users.add(testUser);
        }

        userRepository.save(users);
        return users;
    }

    /**
     * This function creates a set of example divisions.
     * @param availableAdmins A list of available administrator, randomly user are selected to become administrator of divisions.
     * @param rootDivision The current root division.
     * @return A list containing all created divisions.
     */
    private List<Division> createExampleDivision(List<User> availableAdmins, Division rootDivision)
    {
        ArrayList<Division> divisions = new ArrayList<>();
        Random r = new Random();
        divisions.add(new Division("Rugby", null, availableAdmins.get(r.nextInt(availableAdmins.size())), rootDivision));
        divisions.add(new Division("Soccer", null, availableAdmins.get(r.nextInt(availableAdmins.size())), rootDivision));
        divisionRepository.save(divisions); // Need to do this or the referencing is going to fail
        divisions.add(new Division("Rugby - 1st team", null, availableAdmins.get(r.nextInt(availableAdmins.size())), divisions.get(0)));
        divisions.add(new Division("Rugby - 2nd team", null, availableAdmins.get(r.nextInt(availableAdmins.size())), divisions.get(0)));
        divisionRepository.save(divisions);
        return divisions;
    }

    /**
     * This function creates a set of example events.
     * @param availableAdmins A list of available administrator, randomly user are selected to become administrator of events.
     * @param availableDivisions A list of available divisions, randomly divisions are selected to get invited to the event.
     * @return A list of created events.
     */
    private List<Event> createExampleEvents(List<User> availableAdmins, List<Division> availableDivisions)
    {
        Random r = new Random();
        ArrayList<Event> events = new ArrayList<>();
        int thisMonth = LocalDateTime.now().getMonthValue();
        int thisYear = LocalDateTime.now().getYear();

        Event testEvent = new Event();
        testEvent.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 20, 13, 0));
        testEvent.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 20, 14, 0));
        testEvent.setName("Super Event 1");
        testEvent.setLocation("DHBW Stuttgart");
        testEvent.setLocationLat(48.7735272);
        testEvent.setLocationLng(9.171102399999995);
        testEvent.setDescription("Super event at awesome location with great people");
        testEvent.addDivision(availableDivisions.get(r.nextInt(availableDivisions.size())));
        testEvent.setEventAdmin(availableAdmins.get(r.nextInt(availableAdmins.size())));
        testEvent.setLastChanged(LocalDateTime.now());

        events.add(testEvent);

        testEvent = new Event();
        testEvent.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 20, 13, 0));
        testEvent.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 21, 13, 0));
        testEvent.setName("Super Event 2");
        testEvent.addDivision(availableDivisions.get(r.nextInt(availableDivisions.size())));
        testEvent.setEventAdmin(availableAdmins.get(r.nextInt(availableAdmins.size())));
        testEvent.setLastChanged(LocalDateTime.now());

        events.add(testEvent);

        testEvent = new Event();
        testEvent.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 21, 13, 0));
        testEvent.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 21, 13, 5));
        testEvent.setName("Super Event 3");
        testEvent.addDivision(availableDivisions.get(r.nextInt(availableDivisions.size())));
        testEvent.setEventAdmin(availableAdmins.get(r.nextInt(availableAdmins.size())));
        testEvent.setLastChanged(LocalDateTime.now());

        events.add(testEvent);

        testEvent = new Event();
        testEvent.setStartDateTime(LocalDateTime.of(thisYear, thisMonth, 11, 13, 0));
        testEvent.setEndDateTime(LocalDateTime.of(thisYear, thisMonth, 15, 13, 5));
        testEvent.setName("Super Event 4");
        testEvent.addDivision(availableDivisions.get(r.nextInt(availableDivisions.size())));
        testEvent.setEventAdmin(availableAdmins.get(r.nextInt(availableAdmins.size())));
        testEvent.setLastChanged(LocalDateTime.now());

        events.add(testEvent);

        eventRepository.save(events);
        return events;
    }

    /**
     * This function adds a random amount of divisions to every user.
     * @param availableUser The list of available user.
     * @param availableDivision The list of available division.
     */
    private void subscribeUserToDivision(List<User> availableUser, List<Division> availableDivision)
    {
        Random r = new Random();
        for(User user: availableUser)
        {
            int iteration = r.nextInt(availableDivision.size());
            HashSet<Division> divisionHashSet = new HashSet<>();
            for(int i = 0; i<= iteration; i++)
            {
                divisionHashSet.add(availableDivision.get(r.nextInt(availableDivision.size())));
            }
            user.replaceDivisions(divisionRepository, eventRepository, divisionHashSet.stream().collect(Collectors.toList()));
        }
        userRepository.save(availableUser);
    }

    /**
     * This function creates a set of example messages.
     * @param availableDivision A list of available division. Each division is going to get a set of messages.
     * @return The list of created messages.
     */
    private List<Message> createExampleMessages(List<Division> availableDivision)
    {
        Random r = new Random();
        final int maxMessagesPerDiv = 10;
        List<Message> messages = new ArrayList<>();

        // Create messages for each division.
        for(Division division: availableDivision)
        {
            Message testMessage;
            List <String> memberList = division.getMemberList();
            if(memberList != null && !memberList.isEmpty())
            {
                // Create a random amount of messages for this division
                for (int i = r.nextInt(maxMessagesPerDiv); i >= 0; i--)
                {
                    // Select a random sender who is part of the division.
                    User thisSender = userRepository.findById(memberList.get(r.nextInt(memberList.size())));
                    // Create a random message
                    switch (r.nextInt(5))
                    {
                        case 0:
                            testMessage = new Message("Hello world", thisSender, division);
                            break;
                        case 1:
                            testMessage = new Message("Example messages are a cool thing", thisSender, division);
                            break;
                        case 2:
                            testMessage = new Message("Very convenient thing!", thisSender, division);
                            break;
                        case 3:
                            testMessage = new Message("I love it!", thisSender, division);
                            break;
                        case 4:
                            testMessage = new Message("Did you know an unprotected human can survive up to 2 minutes in space?", thisSender, division);
                            break;
                        default:
                            testMessage = new Message("This message should never appear, if it did: Hi to everyone :)", thisSender, division);
                            break;
                    }
                    messages.add(testMessage);
                }
            } else
            {
                logger.warn("Member list for division {} is empty", division);
            }
        }

        messageRepository.save(messages);
        return messages;
    }

    /**
     * This function is resetting the current collection in the database.
     */
    private boolean resetDatabase()
    {
        try
        {
            logger.debug("Trying to drop the current collection.");
            mongoTemplate.getDb().dropDatabase();
            logger.debug("Successfully dropped current collection");
            return true;
        } catch (MongoTimeoutException e)
        {
            logger.debug("Unable to drop database, because the database is not available: {}", e.getMessage());
            return false;
        }
    }
}
