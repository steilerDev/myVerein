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
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
     * This request mapping is processing the request to view the user management page.
     * @return The path to the view for the user management page.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String showUserManagement()
    {
        return "user";
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody ResponseEntity saveUser(@RequestParam String email,
                                                 @RequestParam String firstName,
                                                 @RequestParam String lastName,
                                                 @RequestParam String birthday,
                                                 @RequestParam String memberSince,
                                                 @RequestParam(required = false) String passiveSince,
                                                 @RequestParam String divisions,
                                                 @RequestParam(required = false) String newUser,
                                                 @RequestParam(required = false) String oldUser,
                                                 @RequestParam(required = false) String password,
                                                 @RequestParam Map<String, String> parameters,
                                                 @CurrentUser User currentUser, Locale locale)
    {
        //System.err.println(locale);
        User newUserObject;
        User oldUserObject = null;
        //The user needs always an email identifier
        if(email.isEmpty())
        {
            logger.warn("The email can not be empty");
            return new ResponseEntity("The email can not be empty", HttpStatus.BAD_REQUEST);
        }

        //A new user is added
        if(newUser != null && !newUser.isEmpty())
        {
            logger.debug("A new user is created");
            if(userRepository.findByEmail(parameters.get("email")) == null)
            {
                newUserObject = new User();
                if(password == null || password.isEmpty())
                {
                    logger.warn("The password can not be empty");
                    return new ResponseEntity("The password can not be empty", HttpStatus.BAD_REQUEST);
                } else
                {
                    newUserObject.setPassword(password);
                }
            } else
            {
                logger.warn("A user with the given email already exists.");
                return new ResponseEntity("A user with the given email already exists.", HttpStatus.BAD_REQUEST);
            }
        //An existing user is modified
        } else if (oldUser != null && !oldUser.isEmpty())
        {
            logger.debug("An existing user is modified");
            //The email did not change
            if(oldUser.equals(email) && userRepository.findByEmail(email) != null)
            {
                newUserObject = userRepository.findByEmail(oldUser);
            //Email did change and the new email is unused
            } else if (userRepository.findByEmail(oldUser) != null && userRepository.findByEmail(email) == null)
            {
                logger.debug("The user changed his email");
                oldUserObject = userRepository.findByEmail(oldUser);
                newUserObject = userRepository.findByEmail(oldUser);
            } else
            {
                logger.warn("Problem finding existing user, either the existing user could not be located or the new email is already taken");
                return new ResponseEntity("Problem finding existing user, either the existing user could not be located or the new email is already taken", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.warn("Neither new nor existing user parameter or identifier existing.");
            return new ResponseEntity<>("Neither new nor existing user parameter or identifier existing.", HttpStatus.BAD_REQUEST);
        }
        if(isAllowedToAdministrate(currentUser, newUserObject))
        {
            if (newUserObject == null || firstName.isEmpty() || lastName.isEmpty())
            {
                logger.warn("Required parameter missing.");
                return new ResponseEntity<>("Required parameter missing", HttpStatus.BAD_REQUEST);
            }
            newUserObject.setFirstName(firstName);
            newUserObject.setLastName(lastName);
            newUserObject.setEmail(email);

            if (!birthday.isEmpty())
            {
                try
                {
                    newUserObject.setBirthday(LocalDate.parse(birthday, DateTimeFormatter.ofPattern("dd/MM/YYYY")));
                } catch (DateTimeParseException e)
                {
                    logger.warn("Unrecognized date format (" + birthday + ")");
                    return new ResponseEntity<>("Wrong date format", HttpStatus.BAD_REQUEST);
                }
            } else
            {
                newUserObject.setBirthday(null);
            }

            if (!memberSince.isEmpty())
            {
                try
                {
                    newUserObject.setMemberSince(LocalDate.parse(memberSince, DateTimeFormatter.ofPattern("dd/MM/YYYY")));
                } catch (DateTimeParseException e)
                {
                    logger.warn("Unrecognized date format (" + parameters.get("memberSince") + ")");
                    return new ResponseEntity<>("Wrong date format", HttpStatus.BAD_REQUEST);
                }
            } else
            {
                newUserObject.setBirthday(null);
            }

            if (!divisions.isEmpty())
            {
                String[] divArray = divisions.split(",");
                for (String division : divArray)
                {
                    Division div = divisionRepository.findByName(division);
                    if (div == null)
                    {
                        logger.warn("Unrecognized division (" + div + ")");
                        return new ResponseEntity<>("Division " + div + " does not exist", HttpStatus.BAD_REQUEST);
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
        logger.warn("The user is not allowed to perform these changes.");
        return new ResponseEntity<>("The user is not allowed to perform these changes.", HttpStatus.FORBIDDEN);
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

        if(!isAllowedToAdministrate(currentUser, searchedUser))
        {
            logger.debug("Currently logged in user is not administrating selected user. Hiding private information");
            searchedUser.setPrivateInformation(null);
            searchedUser.setAdministrationAllowed(false);
        } else
        {
            searchedUser.setAdministrationAllowed(true);
        }
        //Not setting admin user null could lead to an infinite loop when creating the JSON
        searchedUser.getDivisions().parallelStream().forEach(div -> div.setAdminUser(null));
        return searchedUser;
    }

    /**
     * This function checks if the currently logged in user is allowed to administrate (view private information and change user details) the selected user.
     * @param currentUser The currently logged in user.
     * @param selectedUser The selected user.
     * @return True if the user is allowed, false otherwise.
     */
    private boolean isAllowedToAdministrate(User currentUser, User selectedUser)
    {
        //Getting the list of administrated divisions
        List<Division> administratedDivisions = divisionRepository.findByAdminUser(currentUser);

        boolean allowedToAdministrate;

        //Is the user does not have any divisions at the moment, the admin is allowed to administrate the user.
        if(selectedUser.getDivisions() == null || selectedUser.getDivisions().isEmpty() || selectedUser.getEmail().equals(currentUser.getEmail()))
        {
            allowedToAdministrate = true;
        } else
        {
            //Checking if the current administrator is administrating any of the divisions the user is part of
            allowedToAdministrate = selectedUser.getDivisions().parallelStream() //Streaming all divisions the user is part of
                    .anyMatch(div -> //If there is any match the admin is allowed to view the user
                            div.getAncestors().parallelStream() //Streaming all ancestors of the user's divisions
                                    .anyMatch(anc -> administratedDivisions.contains(anc))); //If there is any match between administrated divisions and ancestors of one of the users divisions
        }
        return allowedToAdministrate;
    }
}
