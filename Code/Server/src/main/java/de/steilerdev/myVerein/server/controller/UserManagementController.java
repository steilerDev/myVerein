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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * This controller is processing all requests associated with the user management.
 */
@Controller
@RequestMapping("/user")
public class UserManagementController
{
    @Autowired
    DivisionRepository divisionRepository;

    @Autowired
    UserRepository userRepository;

    /**
     * This request mapping is processing the request to view the user management page. It gathers all users displayed within this view.
     * @param model The model handed over to the view.
     * @param currentUser The currently logged in user.
     * @return The path to the view for the user management page.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String showUserManagement(ModelMap model, @CurrentUser User currentUser)
    {
        List<User> allUser = userRepository.findAllEmailAndName();
        model.addAttribute("users", allUser);
        return "user";
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
