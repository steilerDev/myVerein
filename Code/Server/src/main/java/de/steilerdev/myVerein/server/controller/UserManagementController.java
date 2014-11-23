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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    public @ResponseBody ResponseEntity saveUser(@RequestParam Map<String, String> parameters, @CurrentUser User currentUser, Locale locale)
    {
        //Todo: Check if user is administrating the new assigned division
        //System.err.println(locale);
        User newUser;
        if(parameters.get("newUser") != null && !parameters.get("newUser").isEmpty())
        {
            if(userRepository.findByEmail(parameters.get("email")) == null)
            {
                newUser = new User();
                if(parameters.get("password") == null || parameters.get("password").isEmpty())
                {
                    logger.warn("The password can not be empty");
                    return new ResponseEntity("The password can not be empty", HttpStatus.BAD_REQUEST);
                } else
                {
                    newUser.setPassword(parameters.get("password"));
                }
            } else
            {
                logger.warn("A user with the given email already exists.");
                return new ResponseEntity("A user with the given email already exists.", HttpStatus.BAD_REQUEST);
            }
        } else {
            newUser = userRepository.findByEmail(parameters.get("email"));
        }
        if(isAllowedToAdministrate(currentUser, newUser))
        {
            if (newUser == null || parameters.get("firstName") == null || parameters.get("firstName").isEmpty() || parameters.get("lastName") == null || parameters.get("lastName").isEmpty() || parameters.get("email") == null || parameters.get("email").isEmpty() || parameters.get("birthday") == null || parameters.get("memberSince") == null || parameters.get("divisions") == null)
            {
                logger.warn("Required parameter missing.");
                return new ResponseEntity<>("Required parameter missing", HttpStatus.BAD_REQUEST);
            }
            newUser.setFirstName(parameters.get("firstName"));
            newUser.setLastName(parameters.get("lastName"));
            newUser.setEmail(parameters.get("email"));

            if (!parameters.get("birthday").isEmpty())
            {
                try
                {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
                    newUser.setBirthday(dateFormat.parse(parameters.get("birthday")));
                } catch (ParseException e)
                {
                    logger.warn("Unrecognized date format (" + parameters.get("birthday") + ")");
                    return new ResponseEntity<>("Wrong date format", HttpStatus.BAD_REQUEST);
                }
            } else
            {
                newUser.setBirthday(null);
            }

            if (!parameters.get("memberSince").isEmpty())
            {
                try
                {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("DD/MM/YYYY");
                    newUser.setMemberSince(dateFormat.parse(parameters.get("memberSince")));
                } catch (ParseException e)
                {
                    logger.warn("Unrecognized date format (" + parameters.get("memberSince") + ")");
                    return new ResponseEntity<>("Wrong date format", HttpStatus.BAD_REQUEST);
                }
            } else
            {
                newUser.setBirthday(null);
            }

            if (!parameters.get("divisions").isEmpty())
            {
                String[] divisions = parameters.get("divisions").split(",");
                for (String division : divisions)
                {
                    Division div = divisionRepository.findByName(division);
                    if (div == null)
                    {
                        logger.warn("Unrecognized division (" + div + ")");
                        return new ResponseEntity<>("Division does not exist", HttpStatus.BAD_REQUEST);
                    }
                    newUser.addDivision(div);
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
                        newUser.addPrivateInformation(normalizedKey, parameters.get(key));
                    } else if (normalizedKey.startsWith("publicInformation"))
                    {
                        normalizedKey = normalizedKey.substring(17);
                        newUser.addPublicInformation(normalizedKey, parameters.get(key));
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
                        newUser.addPrivateInformation(parameters.get(key), parameters.get(value));
                    } else if (normalizedKey.startsWith("publicInformation"))
                    {
                        String value = key.replace("_key", "_value");
                        newUser.addPublicInformation(parameters.get(key), parameters.get(value));
                    } else
                    {
                        logger.warn("Unrecognized custom field (" + key + ").");
                    }
                }
            });

            try
            {
                userRepository.save(newUser);
            } catch (ConstraintViolationException e)
            {
                logger.warn("A database constraint was violated while saving the user.");
                return new ResponseEntity<>("A database constraint was violated while saving the user.", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        logger.warn("The user is not allowed to perfom these changes.");
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
        if(selectedUser.getDivisions() == null || selectedUser.getDivisions().isEmpty() || selectedUser.getUsername().equals(currentUser.getUsername()))
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
