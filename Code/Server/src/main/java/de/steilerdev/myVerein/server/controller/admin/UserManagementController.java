/**
 * Copyright (C) 2014 Frank Steiler <frank@steilerdev.de>
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

import de.steilerdev.myVerein.server.model.*;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This controller is processing all requests associated with the user management.
 */
@RestController
@RequestMapping("/api/admin/user")
public class UserManagementController
{
    private static Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    DivisionRepository divisionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SettingsRepository settingsRepository;

    @Autowired
    EventRepository eventRepository;

    /**
     * These final strings are used to bundle information within a response entity.
     */
    final String successMessage = "successMessage", errorMessage = "errorMessage", userId = "userId";

    /**
     * If a modification on a division needs to be stored durable, this function is invoked by POSTing the parameters to the URI /api/admin/user.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email of the user.
     * @param birthday The birthday of the user (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param password The initial password of a new user (Only allowed for a new user).
     * @param gender The gender of the user.
     * @param street The street of the user's address.
     * @param streetNumber The street number of the user's address.
     * @param zip The zip code of the user's address.
     * @param city The city of the user's address.
     * @param country The country of the user.
     * @param activeMemberSince The date, when the user became an active member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param passiveMemberSince The date, when the user became a passive member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param resignationDate The date, when the user resigned (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param iban The IBAN of the user's bank account.
     * @param bic The BIC of the user's bank account.
     * @param divisions A comma separated list of divisions, the user is part of.
     * @param userFlag If it is a new user, the user flag is "true", otherwise it is holding the identifier (id) of the user, in case it changed.
     * @param parameters The complete map of all parameters, containing the custom user fields.
     * @param currentUser The currently logged in user.
     * @return A response entity containing a map with either a success message (key: 'successMessage') and the user id of the user (key: 'userId') or an error message (key 'errorMessage'). The appropriate http code is used.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> saveUser(@RequestParam String firstName,
                                           @RequestParam String lastName,
                                           @RequestParam String email,
                                           @RequestParam String birthday,
                                           @RequestParam String password,
                                           @RequestParam(required = false) String gender,
                                           @RequestParam String street,
                                           @RequestParam String streetNumber,
                                           @RequestParam String zip,
                                           @RequestParam String city,
                                           @RequestParam String country,
                                           @RequestParam String activeMemberSince,
                                           @RequestParam String passiveMemberSince,
                                           @RequestParam String resignationDate,
                                           @RequestParam String iban,
                                           @RequestParam String bic,
                                           @RequestParam String divisions,
                                           @RequestParam String userFlag,
                                           @RequestParam Map<String, String> parameters,
                                           @CurrentUser User currentUser)
    {
        logger.trace("[{}] Starting to save a user", currentUser);
        Map<String, String> responseMap = new HashMap<>();

        if (email.isEmpty() || userFlag.isEmpty())
        {
            logger.warn("[{}] The email/user flag can not be empty", currentUser);
            responseMap.put(errorMessage, "The email can not be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if (userFlag.equals("true"))
        {
            return createNewUser(firstName, lastName, email, birthday, password, gender, street, streetNumber, zip, city, country, activeMemberSince, passiveMemberSince, resignationDate, iban, bic, divisions, parameters, currentUser);
        } else
        {
            return modifyExistingUser(firstName, lastName, email, birthday, gender, street, streetNumber, zip, city, country, activeMemberSince, passiveMemberSince, resignationDate, iban, bic, divisions, userFlag, parameters, currentUser);
        }
    }

    /**
     * This function creates a new user, using the provided parameter.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email of the user.
     * @param birthday The birthday of the user (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param password The initial password of a new user (Only allowed for a new user).
     * @param gender The gender of the user.
     * @param street The street of the user's address.
     * @param streetNumber The street number of the user's address.
     * @param zip The zip code of the user's address.
     * @param city The city of the user's address.
     * @param country The country of the user.
     * @param activeMemberSince The date, when the user became an active member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param passiveMemberSince The date, when the user became a passive member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param resignationDate The date, when the user resigned (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param iban The IBAN of the user's bank account.
     * @param bic The BIC of the user's bank account.
     * @param divisions A comma separated list of divisions, the user is part of.
     * @param parameters The complete map of all parameters, containing the custom user fields.
     * @param currentUser The currently logged in user.
     * @return A response entity containing a map with either a success message (key: 'successMessage') and the user id of the user (key: 'userId') or an error message (key 'errorMessage'). The appropriate http code is used.
     */
    private ResponseEntity<Map<String, String>> createNewUser( String firstName,
                                                               String lastName,
                                                               String email,
                                                               String birthday,
                                                               String password,
                                                               String gender,
                                                               String street,
                                                               String streetNumber,
                                                               String zip,
                                                               String city,
                                                               String country,
                                                               String activeMemberSince,
                                                               String passiveMemberSince,
                                                               String resignationDate,
                                                               String iban,
                                                               String bic,
                                                               String divisions,
                                                               Map<String, String> parameters,
                                                               User currentUser)
    {
        User modifyingUser;
        Map<String, String> responseMap = new HashMap<>();

        logger.debug("[{}] A new user is created using the email {}", currentUser, email);
        if (userRepository.findByEmail(email) != null)
        {
            logger.warn("[{}] A user with the given email {} already exists", currentUser, email);
            responseMap.put(errorMessage, "A user with the given email already exists");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(password.isEmpty())
        {
            logger.warn("[{}] The password of the new user {} can not be empty", currentUser, email);
            responseMap.put(errorMessage, "The password can not be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else
        {
            modifyingUser = new User();
            modifyingUser.setPassword(password);
            return populateUser(firstName, lastName, email, birthday, gender, street, streetNumber, zip, city, country, activeMemberSince, passiveMemberSince, resignationDate, iban, bic, divisions, parameters, modifyingUser, currentUser);
        }
    }

    /**
     * This function modifies an existing user
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email of the user.
     * @param birthday The birthday of the user (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param gender The gender of the user.
     * @param street The street of the user's address.
     * @param streetNumber The street number of the user's address.
     * @param zip The zip code of the user's address.
     * @param city The city of the user's address.
     * @param country The country of the user.
     * @param activeMemberSince The date, when the user became an active member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param passiveMemberSince The date, when the user became a passive member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param resignationDate The date, when the user resigned (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param iban The IBAN of the user's bank account.
     * @param bic The BIC of the user's bank account.
     * @param divisions A comma separated list of divisions, the user is part of.
     * @param userId The user id of the user that should be modified.
     * @param parameters The complete map of all parameters, containing the custom user fields.
     * @param currentUser The currently logged in user.
     * @return A response entity containing a map with either a success message (key: 'successMessage') and the user id of the user (key: 'userId') or an error message (key 'errorMessage'). The appropriate http code is used.
     */
    private ResponseEntity<Map<String, String>> modifyExistingUser(String firstName,
                                                                   String lastName,
                                                                   String email,
                                                                   String birthday,
                                                                   String gender,
                                                                   String street,
                                                                   String streetNumber,
                                                                   String zip,
                                                                   String city,
                                                                   String country,
                                                                   String activeMemberSince,
                                                                   String passiveMemberSince,
                                                                   String resignationDate,
                                                                   String iban,
                                                                   String bic,
                                                                   String divisions,
                                                                   String userId,
                                                                   Map<String, String> parameters,
                                                                   User currentUser)
    {
        User modifyingUser;
        Map<String, String> responseMap = new HashMap<>();
        logger.debug("[{}] An existing user {} is modified", currentUser, userId);
        if((modifyingUser = userRepository.findById(userId)) == null)
        {
            logger.warn("[{}] Unable to find existing user object", currentUser);
            responseMap.put(errorMessage, "Unable to retrieve the specified user");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(!modifyingUser.getEmail().equals(email) && userRepository.findByEmail(email) != null)
        {
            logger.warn("[{}] The user's email changed and the new email {} is already taken", currentUser, email);
            responseMap.put(errorMessage, "The new email is already taken");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if (!currentUser.isAllowedToAdministrate(modifyingUser, divisionRepository))
        {
            logger.warn("[{}] The user is not allowed to modify the user {}", currentUser, modifyingUser);
            responseMap.put(errorMessage, "You are not allowed to perform these changes");
            return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
        } else
        {
            return populateUser(firstName, lastName, email, birthday, gender, street, streetNumber, zip, city, country, activeMemberSince, passiveMemberSince, resignationDate, iban, bic, divisions, parameters, modifyingUser, currentUser);
        }
    }

    /**
     * This function populates a given user, using the provided parameters.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email of the user.
     * @param birthday The birthday of the user (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param gender The gender of the user.
     * @param street The street of the user's address.
     * @param streetNumber The street number of the user's address.
     * @param zip The zip code of the user's address.
     * @param city The city of the user's address.
     * @param country The country of the user.
     * @param activeMemberSince The date, when the user became an active member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param passiveMemberSince The date, when the user became a passive member of the club (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param resignationDate The date, when the user resigned (Formatted: d/MM/y according to Java 8 DateTimeFormatter).
     * @param iban The IBAN of the user's bank account.
     * @param bic The BIC of the user's bank account.
     * @param divisions A comma separated list of divisions, the user is part of.
     * @param parameters The complete map of all parameters, containing the custom user fields.
     * @param currentUser The currently logged in user.
     * @param modifyingUser The user that should be modified
     * @return A response entity containing a map with either a success message (key: 'successMessage') and the user id of the user (key: 'userId') or an error message (key 'errorMessage'). The appropriate http code is used.
     */
    private ResponseEntity<Map<String, String>> populateUser(String firstName,
                                                             String lastName,
                                                             String email,
                                                             String birthday,
                                                             String gender,
                                                             String street,
                                                             String streetNumber,
                                                             String zip,
                                                             String city,
                                                             String country,
                                                             String activeMemberSince,
                                                             String passiveMemberSince,
                                                             String resignationDate,
                                                             String iban,
                                                             String bic,
                                                             String divisions,
                                                             Map<String, String> parameters,
                                                             User modifyingUser,
                                                             User currentUser)
    {
        Map<String, String> responseMap = new HashMap<>();
        logger.debug("[{}] Parsing parameters and updating user", currentUser);

        // Parsing mandatory fields

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty())
        {
            logger.warn("[{}] Required parameter missing", currentUser);
            responseMap.put(errorMessage, "Required parameter missing");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        }
        modifyingUser.setFirstName(firstName);
        modifyingUser.setLastName(lastName);
        modifyingUser.setEmail(email);

        // Parsing simple optional fields

        modifyingUser.setStreet(street);
        modifyingUser.setStreetNumber(streetNumber);
        modifyingUser.setZipCode(zip);
        modifyingUser.setCity(city);
        modifyingUser.setCountry(country);
        modifyingUser.setIban(iban);
        modifyingUser.setBic(bic);

        // Parsing complicated optional fields (dates, enums)

        if (!birthday.isEmpty())
        {
            logger.debug("[{}] Parsing birthday for {}", currentUser, modifyingUser);
            try
            {
                modifyingUser.setBirthday(LocalDate.parse(birthday, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[{}] Unrecognized date format ({})", currentUser, birthday);
                responseMap.put(errorMessage, "Wrong date format within birthday field");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[{}] Clearing birthday field for {}", currentUser, modifyingUser);
            modifyingUser.setBirthday(null);
        }

        if (gender != null && !gender.isEmpty() && !gender.equals("default"))
        {
            logger.debug("[{}] Parsing gender for {}", currentUser, modifyingUser);
            try
            {
                modifyingUser.setGender(User.Gender.valueOf(gender));
            } catch (IllegalArgumentException e)
            {
                logger.warn("[{}] Unable to parse gender: {}", currentUser, e.getMessage());
                responseMap.put(errorMessage, "Unable to parse gender");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[{}] Clearing gender field for {}", currentUser, modifyingUser);
            modifyingUser.setGender(null);
        }

        if(!activeMemberSince.isEmpty())
        {
            logger.debug("[{}] Parsing active member field for {}", currentUser, modifyingUser);
            try
            {
                modifyingUser.setActiveSince(LocalDate.parse(activeMemberSince, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[{}] Unrecognized date format ({})", currentUser, activeMemberSince);
                responseMap.put(errorMessage, "Wrong date format within active member field");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[{}] Clearing active member field for {}", currentUser, modifyingUser);
            modifyingUser.setActiveSince(null);
        }

        if(!passiveMemberSince.isEmpty())
        {
            logger.debug("[{}] Parsing passive member field for {}", currentUser, modifyingUser);
            try
            {
                modifyingUser.setPassiveSince(LocalDate.parse(passiveMemberSince, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[{}] Unrecognized date format ({})", currentUser, passiveMemberSince);
                responseMap.put(errorMessage, "Wrong date format within passive member field");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[{}] Clearing passive member field for {}", currentUser, modifyingUser);
            modifyingUser.setPassiveSince(null);
        }

        if(!resignationDate.isEmpty())
        {
            logger.debug("[{}] Parsing resignation date field for {}", currentUser, modifyingUser);
            try
            {
                modifyingUser.setResignationDate(LocalDate.parse(resignationDate, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[{}] Unrecognized date format ({})", currentUser, resignationDate);
                responseMap.put(errorMessage, "Wrong date format within resignation date field");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[{}] Clearing resignation date field for {}", currentUser, modifyingUser);
            modifyingUser.setResignationDate(null);
        }

        if (!divisions.isEmpty())
        {
            logger.debug("[{}] Parsing divisions for {}", currentUser, modifyingUser);
            String[] divArray = divisions.split(",");
            List<Division> divisionList = new ArrayList<>();
            for (String division : divArray)
            {
                Division div = divisionRepository.findByName(division);
                if (div == null)
                {
                    logger.warn("[{}] Unrecognized division ({})", currentUser, division);
                    responseMap.put(errorMessage, "Division " + division + " does not exist");
                    return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
                } else
                {
                    divisionList.add(div);
                }
            }
            modifyingUser.replaceDivisions(divisionRepository, eventRepository, divisionList);
        } else
        {
            logger.debug("[{}] Clearing divisions for {}", currentUser, modifyingUser);
            modifyingUser.replaceDivisions(divisionRepository, eventRepository, (List<Division>)null);
        }

        logger.debug("[{}] Parsing and setting custom user fields", currentUser);
        modifyingUser.setCustomUserField(parameters.keySet().parallelStream().filter(key -> key.startsWith("cuf_") && !parameters.get(key).trim().isEmpty()) //Filtering all custom user fields, which are not empty
                .collect(Collectors.toMap(key -> key.substring(4), key -> parameters.get(key).trim()))); //Creating map of all fields

        logger.debug("[{}] Saving user {}", currentUser, modifyingUser);

        modifyingUser = userRepository.save(modifyingUser);
        logger.info("[{}] Successfully saved user {}", currentUser, modifyingUser);
        responseMap.put(successMessage, "Successfully saved user");
        responseMap.put(userId, modifyingUser.getId());
        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    /**
     * This function gathers all users. The function is invoked by GETting the URI /api/admin/user.
     * @param term If this parameter is present, only users who are matching the term within their email, first or last name are returned.
     * @return An HTTP response with a status code, together with the JSON list-object of all users (matching the term, if present), or only an error code if an error occurred.
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public ResponseEntity<List<User>> getUserList(@RequestParam(required = false) String term, @CurrentUser User currentUser)
    {
        List<User> userList;
        if(term == null || term.isEmpty())
        {
            logger.trace("[{}] Retrieving all users", currentUser);
            userList = userRepository.findAllEmailAndName();
        } else
        {
            logger.trace("[{}] Retrieving all users using the search term {}", currentUser, term);
            userList = userRepository.findAllEmailAndNameContainingString(term);
        }

        if(userList == null)
        {
            logger.warn("[{}] Unable to get users", currentUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            userList.replaceAll(User::getSendingObjectOnlyEmailNameId);
            logger.info("[{}] Returning user", currentUser);
            return new ResponseEntity<>(userList, HttpStatus.OK);
        }
    }

    /**
     * This function returns a single user as JSON object, respecting the privacy of the user, by only returning information the current user is allowed to see. The function is invoked by GETting the URI /api/admin/user using the parameter email.
     * @param userId The id of the user.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code, together with the JSON object of the user, or only an error code if an error occurred.
     */
    @RequestMapping(produces = "application/json", params = "id", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@RequestParam(value = "id") String userId, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Getting user {}", currentUser, userId);
        User searchedUser;
        if(userId.isEmpty())
        {
            logger.warn("[{}] The id is not allowed to be empty", currentUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if((searchedUser = userRepository.findById(userId)) == null)
        {
            logger.warn("[{}] Unable to retrieve user with the id {}", currentUser, userId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if(!currentUser.isAllowedToAdministrate(searchedUser, divisionRepository))
        {
            logger.warn("[{}] The user is not administrating selected user ({}). Hiding private information", currentUser, searchedUser);
            searchedUser = searchedUser.getSendingObjectNoPrivateInformation();
            searchedUser.setAdministrationNotAllowedMessage("You are not allowed to modify this user, since you are not his administrator");
            logger.info("[{}] Returning user {}", currentUser, searchedUser);
            return new ResponseEntity<>(searchedUser, HttpStatus.OK);
        } else
        {
            logger.debug("[{}] User is administrating selected user ({})", currentUser, searchedUser);
            searchedUser = searchedUser.getSendingObject();
            searchedUser.setAdministrationNotAllowedMessage(null);

            //Adding all custom user fields defined within the settings file to the object
            Settings settings = Settings.loadSettings(settingsRepository);
            if(settings.getCustomUserFields() != null && !settings.getCustomUserFields().isEmpty())
            {
                for(String customField: settings.getCustomUserFields())
                {
                    searchedUser.addCustomUserField(customField, "", false);
                }
            } else
            {
                searchedUser.setCustomUserField(null);
            }
            logger.info("[{}] Returning user {}", currentUser, searchedUser);
            return new ResponseEntity<>(searchedUser, HttpStatus.OK);
        }
    }

    /**
     * This function is deleting a user, defined by his id. The function is invoked by DELETEing the URI /api/admin/user.
     * @param userId The id of the user, who needs to be deleted.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUser(@RequestParam(value = "id") String userId, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Deleting user {}", currentUser, userId);
        User deletedUser;
        if(userId.isEmpty())
        {
            logger.warn("[{}] The id is not allowed to be empty", currentUser);
            return new ResponseEntity<>("The id is not allowed to be empty", HttpStatus.BAD_REQUEST);
        } else if((deletedUser = userRepository.findById(userId)) == null)
        {
            logger.warn("[{}] Unable to find stated user {}", currentUser, userId);
            return new ResponseEntity<>("Unable to find the stated user", HttpStatus.BAD_REQUEST);
        } else if (!currentUser.isAllowedToAdministrate(deletedUser, divisionRepository))
        {
            logger.warn("[{}] The user is not allowed to delete the user {}", currentUser, deletedUser);
            return new ResponseEntity<>("You are not allowed to delete the selected user", HttpStatus.FORBIDDEN);
        } else
        {
            logger.trace("[{}] Deleting user {}", currentUser, deletedUser);
            userRepository.delete(deletedUser);
            logger.info("[{}] Successfully delete the user {}", currentUser, deletedUser);
            return new ResponseEntity<>("Successfully deleted selected user", HttpStatus.OK);
        }
    }
}
