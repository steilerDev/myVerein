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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * If a modification on a division needs to be stored durable, this function is invoked by POSTing the parameters to the URI /api/admin/user.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email (unique identifier) of the user.
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
     * @param divisions A comma seperated list of divisions, the user is part of.
     * @param userFlag If it is a new user, the user flag is "true", otherwise it is holding the identifier (email) of the user, in case it changed.
     * @param parameters The complete map of all parameters, containing the custom user fields.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> saveUser(@RequestParam String firstName,
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
        logger.trace("[" + currentUser + "] Starting to save a user.");
        User userObject;

        logger.debug("[" + currentUser + "] Loading user");

        if (email.isEmpty() || userFlag.isEmpty())
        {
            logger.warn("[" + currentUser + "] The email/user flag can not be empty");
            return new ResponseEntity<>("The email can not be empty", HttpStatus.BAD_REQUEST);
        } else if (userFlag.equals("true"))
        {
            logger.info("[" + currentUser + "] A new user is created using the email " + email);
            if (userRepository.findByEmail(email) == null)
            {
                userObject = new User();
                if (password.isEmpty())
                {
                    logger.warn("[" + currentUser + "] The password of the new user " + email + " can not be empty");
                    return new ResponseEntity<>("The password can not be empty", HttpStatus.BAD_REQUEST);
                } else
                {
                    userObject.setPassword(password);
                }
            } else
            {
                logger.warn("[" + currentUser + "] A user with the given email " + email + " already exists");
                return new ResponseEntity<>("A user with the given email already exists.", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.info("[" + currentUser + "] An existing user " + userFlag + " is modified");
            if((userObject = userRepository.findByEmail(userFlag)) == null)
            {
                logger.warn("[{}] Unable to find existing user object", currentUser);
                return new ResponseEntity<>("Unable to retrieve the user", HttpStatus.BAD_REQUEST);
            } else if(!userObject.getEmail().equals(email) && userRepository.findByEmail(email) != null)
            {
                logger.warn("[{}] The new email {} is already taken", currentUser, email);
                return new ResponseEntity<>("The new email is already taken", HttpStatus.BAD_REQUEST);
            }
        }

        logger.debug("[" + currentUser + "] Checking permissions");

        if (!currentUser.isAllowedToAdministrate(userObject, divisionRepository))
        {
            logger.warn("[" + currentUser + "] The user is not allowed to save the user");
            return new ResponseEntity<>("You are not allowed to perform these changes.", HttpStatus.FORBIDDEN);
        }

        logger.debug("[" + currentUser + "] Parsing parameters and updating user");

        if (firstName.isEmpty() || lastName.isEmpty())
        {
            logger.warn("[" + currentUser + "] Required parameter missing");
            return new ResponseEntity<>("Required parameter missing", HttpStatus.BAD_REQUEST);
        }
        userObject.setFirstName(firstName);
        userObject.setLastName(lastName);
        userObject.setEmail(email);

        if (!birthday.isEmpty())
        {
            logger.debug("[" + currentUser + "] Parsing birthday for " + userObject.getEmail());
            try
            {
                userObject.setBirthday(LocalDate.parse(birthday, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[" + currentUser + "] Unrecognized date format (" + birthday + ")");
                return new ResponseEntity<>("Wrong date format within birthday field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[" + currentUser + "] Clearing birthday field for " + userObject.getEmail());
            userObject.setBirthday(null);
        }

        if (gender != null && !gender.isEmpty() && !gender.equals("default"))
        {
            logger.debug("[" + currentUser + "] Parsing gender for " + userObject.getEmail());
            try
            {
                userObject.setGender(User.Gender.valueOf(gender));
            } catch (IllegalArgumentException e)
            {
                logger.warn("[" + currentUser + "] Unable to parse gender: " + e.getMessage());
                return new ResponseEntity<>("Unable to parse gender", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[" + currentUser + "] Clearing gender field for " + userObject.getEmail());
            userObject.setGender(null);
        }

        userObject.setStreet(street);
        userObject.setStreetNumber(streetNumber);
        userObject.setZipCode(zip);
        userObject.setCity(city);
        userObject.setCountry(country);

        if(!activeMemberSince.isEmpty())
        {
            logger.debug("[" + currentUser + "] Parsing active member field for " + userObject.getEmail());
            try
            {
                userObject.setActiveSince(LocalDate.parse(activeMemberSince, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[" + currentUser + "] Unrecognized date format (" + activeMemberSince + ")");
                return new ResponseEntity<>("Wrong date format within active member field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[" + currentUser + "] Clearing active member field for " + userObject.getEmail());
            userObject.setActiveSince(null);
        }

        if(!passiveMemberSince.isEmpty())
        {
            logger.debug("[" + currentUser + "] Parsing passive member field for " + userObject.getEmail());
            try
            {
                userObject.setPassiveSince(LocalDate.parse(passiveMemberSince, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[" + currentUser + "] Unrecognized date format (" + passiveMemberSince + ")");
                return new ResponseEntity<>("Wrong date format within passive member field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[" + currentUser + "] Clearing passive member field for " + userObject.getEmail());
            userObject.setPassiveSince(null);
        }

        if(!resignationDate.isEmpty())
        {
            logger.debug("[" + currentUser + "] Parsing resignation date field for " + userObject.getEmail());
            try
            {
                userObject.setResignationDate(LocalDate.parse(resignationDate, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("[" + currentUser + "] Unrecognized date format (" + resignationDate + ")");
                return new ResponseEntity<>("Wrong date format within resignation date field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.debug("[" + currentUser + "] Clearing resignation date field for " + userObject.getEmail());
            userObject.setResignationDate(null);
        }

        userObject.setIban(iban);
        userObject.setBic(bic);

        if (!divisions.isEmpty())
        {
            logger.debug("[" + currentUser + "] Parsing divisions for " + userObject.getEmail());
            String[] divArray = divisions.split(",");
            List<Division> divisionList = new ArrayList<>();
            for (String division : divArray)
            {
                Division div = divisionRepository.findByName(division);
                if (div == null)
                {
                    logger.warn("[" + currentUser + "] Unrecognized division (" + division + ")");
                    return new ResponseEntity<>("Division " + division + " does not exist", HttpStatus.BAD_REQUEST);
                } else
                {
                    divisionList.add(div);
                }
            }
            userObject.replaceDivisions(divisionRepository, eventRepository, divisionList);
        } else
        {
            logger.debug("[" + currentUser + "] Clearing divisions for " + userObject.getEmail());
            userObject.replaceDivisions(divisionRepository, eventRepository, (List<Division>)null);
        }

        logger.debug("[" + currentUser + "] Parsing and setting custom user fields");
        userObject.setCustomUserField(parameters.keySet().parallelStream().filter(key -> key.startsWith("cuf_") && !parameters.get(key).trim().isEmpty()) //Filtering all custom user fields, which are not empty
                .collect(Collectors.toMap(key -> key.substring(4), key -> parameters.get(key).trim()))); //Creating map of all fields

        logger.debug("[" + currentUser + "] Saving user " + userObject.getEmail());

        try
        {
            logger.debug("[{}] Saving user {}", currentUser, userObject);
            userRepository.save(userObject);
            logger.info("[" + currentUser + "] Successfully saved user " + userObject.getEmail());
            return new ResponseEntity<>("Successfully saved user", HttpStatus.OK);
        } catch (ConstraintViolationException e)
        {
            logger.warn("[" + currentUser + "] A database constraint was violated while saving the user: " + e.getMessage());
            return new ResponseEntity<>("A database constraint was violated while saving the user.", HttpStatus.BAD_REQUEST);
        }
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
            logger.trace("[" + currentUser + "] Retrieving all users");
            userList = userRepository.findAllEmailAndName();
        } else
        {
            logger.trace("[" + currentUser + "] Retrieving all users using the search term " + term);
            userList = userRepository.findAllEmailAndNameContainingString(term);
        }

        if(userList == null)
        {
            logger.warn("[" + currentUser + "] Unable to get users" + (term != null? " matching term " + term: ""));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            userList.replaceAll(User::getSendingObjectOnlyEmailNameId);
            logger.info("[" + currentUser + "] Returning all users" + (term != null? " matching term " + term: ""));
            return new ResponseEntity<>(userList, HttpStatus.OK);
        }
    }

    /**
     * This function returns a single user as JSON object, respecting the privacy of the user, by only returning information the current user is allowed to see. The function is invoked by GETting the URI /api/admin/user using the parameter email.
     * @param email The email of the user.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code, together with the JSON object of the user, or only an error code if an error occurred.
     */
    @RequestMapping(produces = "application/json", params = "email", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@RequestParam String email, @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Getting user " + email);
        User searchedUser, queriedUser;
        if(email.isEmpty())
        {
            logger.warn("[" + currentUser + "] The email is not allowed to be empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if((queriedUser = userRepository.findByEmail(email)) == null)
        {
            logger.warn("[" + currentUser + "] Unable to retrieve user with the email " + email);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else if(!currentUser.isAllowedToAdministrate(queriedUser, divisionRepository))
        {
            logger.warn("[" + currentUser + "] The user is not administrating selected user (" + queriedUser.getEmail() + "). Hiding private information");
            searchedUser = queriedUser.getSendingObjectNoPrivateInformation();
            searchedUser.setAdministrationNotAllowedMessage("You are not allowed to modify this user, since you are not his administrator");
        } else
        {
            logger.debug("[" + currentUser + "] Currently logged in user (" + currentUser.getEmail() + ") is administrating selected user (" + queriedUser.getEmail() + ")");
            searchedUser = queriedUser.getSendingObject();
            searchedUser.setAdministrationNotAllowedMessage(null);
            //Adding all custom user fields defined within the settings file to the object
            Settings settings = Settings.loadSettings(settingsRepository);
            if(settings.getCustomUserFields() != null && !settings.getCustomUserFields().isEmpty())
            {
                settings.getCustomUserFields().stream().forEach(fieldKey -> searchedUser.addCustomUserField(fieldKey, "", false));
            } else if(searchedUser.getCustomUserField() == null || searchedUser.getCustomUserField().isEmpty())
            {
                searchedUser.setCustomUserField(null);
            }
        }

        logger.info("[" + currentUser + "] Returning user " + searchedUser.getEmail());
        return new ResponseEntity<>(searchedUser, HttpStatus.OK);
    }

    /**
     * This function is deleting a user, defined by his email. The function is invoked by DELETEing the URI /api/admin/user.
     * @param email The email of the user, who needs to be deleted.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUser(@RequestParam String email, @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Deleting user " + email);
        User deletedUser;
        if(email.isEmpty())
        {
            logger.warn("[" + currentUser + "] The email is not allowed to be empty");
            return new ResponseEntity<>("The email is not allowed to be empty", HttpStatus.BAD_REQUEST);
        } else if((deletedUser = userRepository.findByEmail(email)) == null)
        {
            logger.warn("[" + currentUser + "] Unable to find stated user " + email);
            return new ResponseEntity<>("Unable to find the stated user", HttpStatus.INTERNAL_SERVER_ERROR);
        } else if (!currentUser.isAllowedToAdministrate(deletedUser, divisionRepository))
        {
            logger.warn("[" + currentUser + "] The user is not allowed to delete the user " + deletedUser.getEmail());
            return new ResponseEntity<>("You are not allowed to delete the selected user", HttpStatus.BAD_REQUEST);
        } else
        {
            try
            {
                logger.trace("[" + currentUser + "] Deleting user " + deletedUser.getEmail());
                userRepository.delete(deletedUser);
                logger.info("[" + currentUser + "] Successfully delete the user " + deletedUser.getEmail());
                return new ResponseEntity<>("Successfully deleted selected user", HttpStatus.OK);
            } catch (IllegalArgumentException e)
            {
                logger.warn("[" + currentUser + "] Unable to delete selected user: " + e.getMessage());
                return new ResponseEntity<>("Unable to delete the selected user", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
