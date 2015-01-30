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
package de.steilerdev.myVerein.server.controller;

import de.steilerdev.myVerein.server.model.Division;
import de.steilerdev.myVerein.server.model.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.model.UserRepository;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This controller is processing all requests associated with the user management.
 */
@Controller
@RequestMapping("/user")
public class UserManagementController
{
    private static Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    @Autowired
    DivisionRepository divisionRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * This function saves the user, according to the parameters posted through this request.
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveUser(@RequestParam String firstName,
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
                                                 @CurrentUser User currentUser, Locale locale)
    {
        //System.err.println(locale);
        User newUserObject;
        User oldUserObject = null;

        /*

            Loading user

         */

        //The user needs always an email identifier
        if(email.isEmpty() || userFlag.isEmpty())
        {
            logger.warn("The email/user flag can not be empty");
            return new ResponseEntity<>("The email can not be empty", HttpStatus.BAD_REQUEST);
        }

        if(userFlag.equals("true")) //A new user is added
        {
            logger.debug("A new user is created");
            if(userRepository.findByEmail(email) == null)
            {
                newUserObject = new User();
                if(password.isEmpty())
                {
                    logger.warn("The password can not be empty");
                    return new ResponseEntity<>("The password can not be empty", HttpStatus.BAD_REQUEST);
                } else
                {
                    newUserObject.setPassword(password);
                }
            } else
            {
                logger.warn("A user with the given email already exists.");
                return new ResponseEntity<>("A user with the given email already exists.", HttpStatus.BAD_REQUEST);
            }
        } else //An existing user is modified
        {
            logger.debug("An existing user is modified");
            if(userFlag.equals(email) && userRepository.findByEmail(email) != null) //The email did not change
            {
                newUserObject = userRepository.findByEmail(userFlag);
            } else if (userRepository.findByEmail(userFlag) != null && userRepository.findByEmail(email) == null) //Email did change and the new email is unused
            {
                logger.debug("The user changed his email");
                oldUserObject = userRepository.findByEmail(userFlag);
                newUserObject = userRepository.findByEmail(userFlag);
            } else
            {
                logger.warn("A problem occurred while retrieving the user, either the existing user could not be located or the new email is already taken");
                return new ResponseEntity<>("A problem occurred while retrieving the user, either the existing user could not be located or the new email is already taken", HttpStatus.BAD_REQUEST);
            }
        }

        /*

            Checking permissions

         */

        if(!currentUser.isAllowedToAdministrate(newUserObject, divisionRepository))
        {
            logger.warn("The user is not allowed to perform these changes.");
            return new ResponseEntity<>("The user is not allowed to perform these changes.", HttpStatus.FORBIDDEN);
        }

        /*

            Parsing user

         */

        if (newUserObject == null || firstName.isEmpty() || lastName.isEmpty())
        {
            logger.warn("Required parameter missing.");
            return new ResponseEntity<>("Required parameter missing", HttpStatus.BAD_REQUEST);
        }
        newUserObject.setFirstName(firstName);
        newUserObject.setLastName(lastName);
        newUserObject.setEmail(email);

        if(!birthday.isEmpty())
        {
            try
            {
                newUserObject.setBirthday(LocalDate.parse(birthday, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("Unrecognized date format (" + birthday + ")");
                return new ResponseEntity<>("Wrong date format within birthday field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            newUserObject.setBirthday(null);
        }

        if(!gender.isEmpty() && !gender.equals("default"))
        try
        {
            newUserObject.setGender(User.Gender.valueOf(gender));
        } catch (IllegalArgumentException e)
        {
            logger.warn("Unable to parse gender: " + e.getMessage());
            return new ResponseEntity<>("Unable to parse gender", HttpStatus.BAD_REQUEST);
        }

        newUserObject.setStreet(street);
        newUserObject.setStreetNumber(streetNumber);
        newUserObject.setZipCode(zip);
        newUserObject.setCity(city);
        newUserObject.setCountry(country);

        if(!activeMemberSince.isEmpty())
        {
            try
            {
                newUserObject.setActiveSince(LocalDate.parse(activeMemberSince, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("Unrecognized date format (" + activeMemberSince + ")");
                return new ResponseEntity<>("Wrong date format within active member field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            newUserObject.setActiveSince(null);
        }

        if(!passiveMemberSince.isEmpty())
        {
            try
            {
                newUserObject.setPassiveSince(LocalDate.parse(passiveMemberSince, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("Unrecognized date format (" + passiveMemberSince + ")");
                return new ResponseEntity<>("Wrong date format within passive member field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            newUserObject.setPassiveSince(null);
        }

        if(!resignationDate.isEmpty())
        {
            try
            {
                newUserObject.setResignationDate(LocalDate.parse(resignationDate, DateTimeFormatter.ofPattern("d/MM/y")));
            } catch (DateTimeParseException e)
            {
                logger.warn("Unrecognized date format (" + resignationDate + ")");
                return new ResponseEntity<>("Wrong date format within resignation date field", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            newUserObject.setResignationDate(null);
        }

        newUserObject.setIban(iban);
        newUserObject.setBic(bic);

        if (!divisions.isEmpty())
        {
            String[] divArray = divisions.split(",");
            for (String division : divArray)
            {
                Division div = divisionRepository.findByName(division);
                if (div == null)
                {
                    logger.warn("Unrecognized division (" + division + ")");
                    return new ResponseEntity<>("Division " + division + " does not exist", HttpStatus.BAD_REQUEST);
                }
                newUserObject.addDivision(div);
            }
        }

        parameters.keySet().parallelStream().forEach(key -> {
            String normalizedKey;
            if (key.startsWith("_old."))
            {
                normalizedKey = key.substring(5);
                if (normalizedKey.startsWith("privateInformation"))
                {
                    normalizedKey = normalizedKey.substring(18);
                    newUserObject.addPrivateInformation(normalizedKey, parameters.get(key));
                } else if (normalizedKey.startsWith("publicInformation"))
                {
                    normalizedKey = normalizedKey.substring(17);
                    newUserObject.addPublicInformation(normalizedKey, parameters.get(key));
                } else
                {
                    logger.warn("Unrecognized custom field (" + key + ").");
                }
            } else if (key.startsWith("_new.") && key.endsWith("_key"))
            {
                normalizedKey = key.substring(5);
                if (normalizedKey.startsWith("privateInformation"))
                {
                    String value = key.replace("_key", "_value");
                    newUserObject.addPrivateInformation(parameters.get(key), parameters.get(value));
                } else if (normalizedKey.startsWith("publicInformation"))
                {
                    String value = key.replace("_key", "_value");
                    newUserObject.addPublicInformation(parameters.get(key), parameters.get(value));
                } else
                {
                    logger.warn("Unrecognized custom field (" + key + ").");
                }
            }
        });

        /*

            Saving new user/deleting old user

         */

        try
        {
            userRepository.save(newUserObject);
            if(oldUserObject != null)
            {
                userRepository.delete(oldUserObject);
            }
        } catch (ConstraintViolationException e)
        {
            logger.warn("A database constraint was violated while saving the user.");
            return new ResponseEntity<>("A database constraint was violated while saving the user.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Successfully saved user", HttpStatus.OK);
    }

    /**
     * This function gathers all user and returns them. Only the first name, last name and emails are returned.
     * @return A list of all user. The response is converted to json using Jackson converter.
     */
    @RequestMapping(value = "getUser", produces = "application/json")
    public @ResponseBody List<User> getUser(@RequestParam(required = false) String term)
    {
        if(term == null || term.isEmpty())
        {
            return userRepository.findAllEmailAndName();
        } else
        {
            return userRepository.findAllEmailAndNameContainingString(term);
        }
    }

    @RequestMapping(value = "getUser", produces = "application/json", params = "email")
    public @ResponseBody User getUser(@RequestParam String email, @CurrentUser User currentUser)
    {
        User searchedUser = userRepository.findByEmail(email);

        if(!currentUser.isAllowedToAdministrate(searchedUser, divisionRepository))
        {
            logger.debug("Currently logged in user is not administrating selected user. Hiding private information");
            searchedUser.setPrivateInformation(null);
            searchedUser.setBic(null);
            searchedUser.setIban(null);
            searchedUser.setBirthday(null);
            searchedUser.setStreet(null);
            searchedUser.setStreetNumber(null);
            searchedUser.setCity(null);
            searchedUser.setZipCode(null);
            searchedUser.setAdministrationNotAllowedMessage("You are not allowed to modify this user, since you are not his administrator");
        } else
        {
            searchedUser.setAdministrationNotAllowedMessage(null);
        }

        if(searchedUser.getDivisions() != null)
        {
            //Not setting admin user null could lead to an infinite loop when creating the JSON
            searchedUser.getDivisions().parallelStream().forEach(div -> div.setAdminUser(null));
        }
        return searchedUser;
    }

    @RequestMapping(method = RequestMethod.POST, value = "deleteUser")
    public @ResponseBody ResponseEntity<String> deleteUser(@RequestParam String email, @CurrentUser User currentUser)
    {
        if(email.isEmpty())
        {
            logger.warn("The email is not allowed to be empty");
            return new ResponseEntity<>("The email is not allowed to be empty", HttpStatus.BAD_REQUEST);
        }
        User deletedUser = userRepository.findByEmail(email);
        if(deletedUser == null)
        {
            logger.warn("Unable to find stated user " + email);
            return new ResponseEntity<>("Unable to find the stated user", HttpStatus.BAD_REQUEST);
        } else if (!currentUser.isAllowedToAdministrate(deletedUser, divisionRepository))
        {
            logger.warn("Not allowed to delete user.");
            return new ResponseEntity<>("You are not allowed to delete the selected user", HttpStatus.BAD_REQUEST);
        } else
        {
            try
            {
                userRepository.delete(deletedUser);
                return new ResponseEntity<>("Successfully deleted selected user", HttpStatus.OK);
            } catch (IllegalArgumentException e)
            {
                logger.warn("Unable to delete selected user.");
                return new ResponseEntity<>("Unable to delete the selected user", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
