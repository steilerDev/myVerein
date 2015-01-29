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

    @Autowired
    UserRepository userRepository;

    private static Logger logger = LoggerFactory.getLogger(DivisionManagementController.class);

    private static final String newDivisionName = "New division";

    /**
     * If a modification on a division needs to be stored durable this controller is invoked by posting the parameters to the URI /division
     * @param name The new name of the division.
     * @param oldName The old name of the division (might be equal to new name)
     * @param description The description of the division (not required parameter)
     * @param admin The name of the administrating user (not required parameter)
     * @param currentUser The currently logged in user.
     * @return A HTTP response with an status code. If an error occurred an error message is bundled into the response.
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveDivision(@RequestParam String name,
                                                             @RequestParam String oldName,
                                                             @RequestParam String description,
                                                             @RequestParam String admin,
                                                             @CurrentUser User currentUser)
    {
        String successMessage = "Successfully saved division";
        if(currentUser.isAdmin())
        {
            Division division;
            Division oldDivision = null;

            if(oldName.isEmpty())
            {
                logger.warn("The original name of the division is missing");
                return new ResponseEntity<>("The original name of the division is missing", HttpStatus.BAD_REQUEST);
            } else if(oldName.equals(name) && (division = divisionRepository.findByName(oldName)) != null)
            {
                //A division is changed, name stays.
                logger.debug("An existing division is changed. The identification is unchanged.");
            } else if((oldDivision = division = divisionRepository.findByName(oldName)) != null && divisionRepository.findByName(name) == null)
            {
                //An existing divisions name is changed and the name is unique
                logger.debug("An existing division is changed. The identification has changed as well.");
            } else
            {
                return new ResponseEntity<>("Problem finding existing division, either the existing division could not be located or the new name is already taken", HttpStatus.BAD_REQUEST);
            }

            if(currentUser.isAllowedToAdministrate(division)) //Check if user is allowed to change the division (if he administrates one of the parent divisions)
            {
                User adminUser = null;
                if (admin != null && !admin.isEmpty())
                {
                    adminUser = userRepository.findByEmail(admin);
                    if (adminUser == null)
                    {
                        logger.warn("Unable to find specified admin user.");
                        return new ResponseEntity<>("Unable to find specified admin user.", HttpStatus.BAD_REQUEST);
                    }
                }
                division.setAdminUser(adminUser);
                division.setName(name);
                division.setDesc(description);

                try
                {
                    if (oldDivision != null)
                    {
                        logger.debug("Deleting old division.");
                        divisionRepository.delete(oldDivision);
                    }
                    divisionRepository.save(division);
                } catch (ConstraintViolationException e)
                {
                    logger.warn("A database constraint was violated while saving the division.");
                    return new ResponseEntity<>("A database constraint was violated while saving the division.", HttpStatus.BAD_REQUEST);
                }
                logger.debug(successMessage);
                return new ResponseEntity<>(successMessage, HttpStatus.OK);
            } else
            {
                logger.warn("User is not allowed to change declared division.");
                return new ResponseEntity<>("You are not allowed to change this division.", HttpStatus.FORBIDDEN);
            }
        } else
        {
            logger.warn("User is not allowed to create a new division.");
            return new ResponseEntity<>("You are not allowed to create a new division", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * This controller is creating a new division and chooses the name based on the new division name and an integer, depending how many unnamed division exist.
     * @param currentUser The currently logged in user.
     * @return A HTTP response with an status code. If an error occurred an error message is bundled into the response. If the division gets created successfully, the name is returned within the response.
     */
    @RequestMapping(method = RequestMethod.POST, value = "new")
    public @ResponseBody ResponseEntity<String> createDivision(@CurrentUser User currentUser)
    {
        List<Division> administratedDivisions = getOptimizedSetOfAdministratedDivisions(currentUser);
        System.err.println("Size: " + administratedDivisions.size());
        System.err.println("First division " + administratedDivisions.get(0));
        Division newDivision;
        if(administratedDivisions != null && administratedDivisions.size() > 0)
        {
            String newName = newDivisionName;
            for(int i = 1; divisionRepository.findByName(newName) != null; i++)
            {
                newName = newDivisionName.concat(" " + i);
            }

            logger.debug("A new division is created: " + newName);

            newDivision = new Division();
            //If there is a new division the parent is one of the administrated divisions. The correct layout is updated through a different request.
            newDivision.setParent(administratedDivisions.get(0));
            newDivision.setName(newName);
            try
            {
                divisionRepository.save(newDivision);
                return new ResponseEntity<>("The new division was successfully created||" + newName, HttpStatus.OK);
            } catch (ConstraintViolationException e)
            {
                logger.warn("A database constraint was violated while saving the division.");
                return new ResponseEntity<>("A database constraint was violated while saving the division.", HttpStatus.BAD_REQUEST);
            }
        } else
        {
            return new ResponseEntity<>("You are not allowed to create a new division", HttpStatus.FORBIDDEN);
        }
    }


    @RequestMapping(method = RequestMethod.POST, value = "deleteDivision")
    public @ResponseBody ResponseEntity<String> deleteDivision(@RequestParam String divisionName, @CurrentUser User currentUser)
    {
        Division deletedDivision = divisionRepository.findByName(divisionName);
        if(deletedDivision == null)
        {
            logger.warn("Unable to find stated division " + divisionName);
            return new ResponseEntity<>("Unable to find the stated division", HttpStatus.BAD_REQUEST);
        } else if (!currentUser.isAllowedToAdministrate(deletedDivision))
        {
            logger.warn("Not allowed to delete division.");
            return new ResponseEntity<>("You are not allowed to delete the selected division", HttpStatus.BAD_REQUEST);
        } else
        {
            try
            {
                divisionRepository.delete(deletedDivision);
                return new ResponseEntity<>("Successfully deleted selected division", HttpStatus.OK);
            } catch (IllegalArgumentException e)
            {
                logger.warn("Unable to delete selected division.");
                return new ResponseEntity<>("Unable to delete the selected division", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * This function gathers the names of all available divisions and returns them.
     * @param term A term, that is required to be part of the division name.
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
     * This function returns a single division as JSON object, where the administrator's fields are reduced to his name and email.
     * @param name The name of the division.
     * @return A JSON object of the division.
     */
    @RequestMapping(value = "getDivision", produces = "application/json", params = "name")
    public @ResponseBody Division getSingleDivision (@RequestParam String name, @CurrentUser User user)
    {
        Division searchedDivision = divisionRepository.findByName(name);
        if(searchedDivision.getAdminUser() != null)
        {
            searchedDivision.getAdminUser().removeEverythingExceptEmailAndName();
        }
        return searchedDivision;
    }

    /**
     * This controller is invoked as soon as an administrator is changing the layout of the division scheme.
     * @param moved_node The name of the node that has been moved.
     * @param target_node The name of the node, where the moved_node got moved to.
     * @param position The relation the moved_node is positioned to the target_node.
     * @param previous_parent The previous parent of the node.
     * @param currentUser The currently logged in user.
     * @return An HTTP status code and an error message if an error occurred.
     */
    @RequestMapping(value="updateDivisionTree", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveTree(@RequestParam String moved_node,
                                                 @RequestParam String target_node,
                                                 @RequestParam String position,
                                                 @RequestParam String previous_parent,
                                                 @CurrentUser User currentUser)
    {
        if(moved_node.isEmpty() || target_node.isEmpty() || position.isEmpty() || previous_parent.isEmpty())
        {
            logger.warn("Required parameter missing.");
            return new ResponseEntity<>("Required parameter missing", HttpStatus.BAD_REQUEST);
        } else
        {
            //The parameters are not empty, we will need the two objects.
            Division targetDivision = divisionRepository.findByName(target_node);
            Division movedDivision = divisionRepository.findByName(moved_node);

            if(movedDivision == null || targetDivision == null)
            {
                logger.warn("Unable to find new parent or moved division.");
                return new ResponseEntity<>("Unable to find new parent or moved division", HttpStatus.BAD_REQUEST);
            } else if(position.equals("after"))
            {
                if(targetDivision.getParent().equals(movedDivision.getParent()))
                {
                    logger.debug("Position 'after', but target_node's parent equals moved_node's parent. Concluding the layout did not change.");
                    return new ResponseEntity<>("Successfully updated division tree", HttpStatus.OK);
                } else
                {
                    return changeDivisionParent(movedDivision, targetDivision.getParent(), currentUser);
                }
            } else if(position.equals("inside"))
            {
                if(previous_parent.equals(target_node))
                {
                    logger.debug("Previous parent (" + previous_parent + ") is identical to new parent (" + target_node + "). Structure unchanged.");
                    return new ResponseEntity<>("Successfully updated division tree", HttpStatus.OK);
                } else
                {
                    return changeDivisionParent(movedDivision, targetDivision, currentUser);
                }
            } else
            {
                logger.warn("Unrecognized position of the node (" + position + ")");
                return new ResponseEntity<>("Unrecognized position of the nod", HttpStatus.BAD_REQUEST);
            }
        }
    }

    /**
     * This function moves the selected division (and all subdivisions) to a new parent. The function evaluates if the user is allowed to perform this action.
     * @param selectedDivision The division that needs to be moved.
     * @param newParent The new parent for the division.
     * @param currentUser The currently logged in user.
     * @return The response entity for the performed action.
     */
    private ResponseEntity<String> changeDivisionParent(Division selectedDivision, Division newParent, User currentUser)
    {
        logger.debug("Changing layout: " + selectedDivision.getName() + " moved to parent " + newParent.getName());

        //The moved node and the target need to be administrated by the user. The nodes should not be root nodes of the user's administration.
        if(currentUser.isAllowedToAdministrate(newParent) && currentUser.isAllowedToAdministrate(selectedDivision))
        {
            logger.debug("Changing structure.");
            updateSubtree(selectedDivision, newParent);
            return new ResponseEntity<>("Successfully updated division tree", HttpStatus.OK);
        } else
        {
            logger.warn("The user is not allowed to move the node.");
            return new ResponseEntity<>("The user is not allowed to move the node", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * This recursive function updates the complete subtree.
     * @param newChild The new child.
     * @param newParent The new parent.
     */
    private void updateSubtree(Division newChild, Division newParent)
    {
        newChild.setParent(newParent);
        Division newNode = divisionRepository.save(newChild);

        divisionRepository.findByParent(newNode).parallelStream().forEach(div -> updateSubtree(div, newNode));
    }

    /**
     * This function gathers the complete division tree that the user is administrating.
     * @param currentUser The currently logged in user.
     * @return A list of tree nodes, that represent the division tree.
     */
    @RequestMapping(value = "getDivisionTree", produces = "application/json")
    public @ResponseBody List<TreeNode> getDivisionTree(@CurrentUser User currentUser)
    {
        List<Division> divisions = getOptimizedSetOfAdministratedDivisions(currentUser);
        List<TreeNode> divisionTree = new ArrayList<>();
        divisions.parallelStream().forEach(div -> divisionTree.add(getSubTree(div)));
        return divisionTree;
    }

    /**
     * This recursive function gathers the subtree starting at the selected node.
     * @param div The root node of the searched sub tree.
     * @return The sub tree starting at the division node.
     */
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
    public List<Division> getOptimizedSetOfAdministratedDivisions(User currentUser)
    {
        // Checking if user is superadmin, which concludes he would administrate every division.
        return currentUser.isSuperAdmin()
                ? divisionRepository.findByParent(null)
                //? divisionRepository.findByAdminUser(currentUser).parallelStream().filter(div -> div.getParent() == null).collect(Collectors.toList()) //Return the root node if the user is superadmin
                : getOptimizedSetOfDivisions(divisionRepository.findByAdminUser(currentUser)); //Return an optimized set of divisions if he is a normal admin
    }

    /**
     * This function is using a set of divisions, and reduces it to the divisions closest to the root
     * @param unoptimizedSetOfDivisions
     * @return
     */
    public static List<Division> getOptimizedSetOfDivisions(List<Division> unoptimizedSetOfDivisions)
    {
        //Reducing the list to the divisions that are on the top of the tree, removing all unnecessary divisions.
        return unoptimizedSetOfDivisions.stream() //Creating a stream of all divisions
                .filter(division -> unoptimizedSetOfDivisions.stream() //filtering all divisions that are already defined in a divisions that is closer to the root of the tree
                        .noneMatch(allDivisions -> division.getAncestors().contains(allDivisions))) //Checking, if there is any division in the list, that is an ancestor of the current division. If there is a match there exists a closer division.
                .collect(Collectors.toList()); // Converting the stream to a list
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
