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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.steilerdev.myVerein.server.model.Division;
import de.steilerdev.myVerein.server.model.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This controller is processing all requests associated with the division management.
 */
@Controller
@RequestMapping("/division")
public class DivisionManagementController
{
    @Autowired
    DivisionRepository divisionRepository;

    /**
     * This request mapping is processing the request to view the division management page.
     * @return The path to the view for the division management page.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String showDivisionManagement()
    {
        return "division";
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

    @RequestMapping(value = "getDivision", produces = "application/json", params = "name")
    public @ResponseBody Division getSingleDivision (@RequestParam String name)
    {
        Division searchedDivision = divisionRepository.findByName(name);
        searchedDivision.getAdminUser().setPrivateInformation(null);
        searchedDivision.getAdminUser().setPublicInformation(null);
        searchedDivision.getAdminUser().setDivisions(null);
        searchedDivision.getAdminUser().setBirthday(null);
        searchedDivision.getAdminUser().setMemberSince(null);
        return searchedDivision;
    }

    /**
     * This function gathers the complete division tree that the user is administrating.
     * @return A list of treenodes, that represent the division tree.
     */
    @RequestMapping(value = "getDivisionTree", produces = "application/json")
    public @ResponseBody List<TreeNode> getDivisionTree(@CurrentUser User currentUser)
    {
        List<Division> divisions = getOptimizedSetOfAdministratedDivisions(currentUser);
        List<TreeNode> divisionTree = new ArrayList<>();
        divisions.parallelStream().forEach(div -> divisionTree.add(getSubTree(div)));
        return divisionTree;
    }

    public TreeNode getSubTree(Division div)
    {
        List<Division> children = divisionRepository.findByParent(div);
        TreeNode subTree = new TreeNode(div.getName());
        if(!children.isEmpty())
        {
            children.stream().forEach(division -> subTree.addChildren(getSubTree(division)));
        }
        return subTree;
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


    /**
     * This subclass is representing the data structure needed by the jqTree framework
     */
    public class TreeNode
    {
        private String label;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<TreeNode> children;

        public TreeNode(String label)
        {
            this.label = label;
        }

        public void addChildren(TreeNode children)
        {
            if(this.children == null)
            {
                this.children = new ArrayList<TreeNode>();
            }
            this.children.add(children);
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }

        public List<TreeNode> getChildren()
        {
            return children;
        }

        public void setChildren(List<TreeNode> children)
        {
            this.children = children;
        }
    }
}
