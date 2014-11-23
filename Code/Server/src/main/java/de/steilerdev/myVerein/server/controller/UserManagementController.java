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

import java.util.ArrayList;
import java.util.List;
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
    public @ResponseBody ResponseEntity saveUser(@RequestParam Map<String, String> parameters)
    {
        parameters.keySet().stream().forEach(key -> {
            System.err.println("Key: " + key + " Value: " + parameters.get(key));
        });
        return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
    }

    /**
     * This function gathers the names of all available divisions and returns them.
     * @return A list of all names of the available divisions. The response is converted to json using a Jackson converter.
     */
    @RequestMapping(value = "getDivision", produces = "application/json")
    public @ResponseBody List<Division> getDivision(@RequestParam(required = false) String term)
    {
        if(term == null || term.isEmpty())
        {
            return divisionRepository.findAllNames();
        } else
        {
            return divisionRepository.findAllNamesContainingString(term);
        }
    }

    /**
     * This function gathers all user and returns them. Only the first name, last name and emails are returned.
     * @return A list of all user. The response is converted to json using Jackson converter.
     */
    @RequestMapping(value = "getUser", produces = "application/json")
    public @ResponseBody List<User> getUser()
    {
        return userRepository.findAllEmailAndName();
    }

    @RequestMapping(value = "getUser", produces = "application/json", params = "email")
    public @ResponseBody User getUser(@RequestParam String email, @CurrentUser User currentUser)
    {
        User searchedUser = userRepository.findByEmail(email);
        //Getting the list of administrated divisions
        List<Division> administratedDivisions = divisionRepository.findByAdminUser(currentUser);

        boolean allowedToAdministrate;

        //Is the user does not have any divisions at the moment, the admin is allowed to administrate the user.
        if(searchedUser.getDivisions() == null || searchedUser.getDivisions().isEmpty())
        {
            allowedToAdministrate = true;
        } else
        {
            //Checking if the current administrator is administrating any of the divisions the user is part of
            allowedToAdministrate = searchedUser.getDivisions().parallelStream() //Streaming all divisions the user is part of
                    .anyMatch(div -> //If there is any match the admin is allowed to view the user
                            div.getAncestors().parallelStream() //Streaming all ancestors of the user's divisions
                                    .anyMatch(anc -> administratedDivisions.contains(anc))); //If there is any match between administrated divisions and ancestors of one of the users divisions
        }

        if(!allowedToAdministrate)
        {
            logger.debug("Currently logged in user is not administrating selected user. Hiding private information");
            searchedUser.setPrivateInformation(null);
        }
        searchedUser.setAdministrationAllowed(allowedToAdministrate);

        return searchedUser;
    }


    /**
     * This function is collecting all divisions administrated by the user and only returns the divisions that are closest to the root node on their respective paths.
     * @param currentUser The currently logged in user.
     * @return The optimized set of administrated divisions.
     */
    private List<Division> getOptimizedSetOfAdministratedDivisions(User currentUser)
    {
        List<Division> administratedDivisions = divisionRepository.findByAdminUser(currentUser);
        List<Division> reducedDivisions;

        // Checking if user is superadmin, which concludes he would administrate every division.
        if(currentUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPERADMIN")))
        {
            //Finding and returning root node.
            reducedDivisions = administratedDivisions.parallelStream().filter(div -> div.getParent() == null).collect(Collectors.toList());
        } else
        {
            //Reducing the list to the divisions that are on the top of the tree, removing all unnecessary divisions.
            reducedDivisions = administratedDivisions.stream() //Creating a stream of all divisions
                    .filter(division -> administratedDivisions.stream() //filtering all divisions that are already defined in a divisions that is closer to the root of the tree
                            .noneMatch(allDivisions -> division.getAncestors().contains(allDivisions))) //Checking, if there is any division in the list, that is an ancestor of the current division. If there is a match there exists a closer division.
                    .collect(Collectors.toList()); // Converting the stream to a list
        }
        return reducedDivisions;
    }
}
