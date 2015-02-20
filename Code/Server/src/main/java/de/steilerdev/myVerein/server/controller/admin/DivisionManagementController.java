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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This controller is processing all requests associated with the division management.
 */
@Controller
@RequestMapping("/api/admin/division")
public class DivisionManagementController
{
    @Autowired
    DivisionRepository divisionRepository;

    @Autowired
    UserRepository userRepository;

    private static Logger logger = LoggerFactory.getLogger(DivisionManagementController.class);

    /**
     * The name given to new divisions
     */
    private static final String newDivisionName = "New division";

    /**
     * This function is saving changes on an exisiting division. If the division needs to be created see {@link DivisionManagementController#createDivision}. This function is invoked by POSTing the parameters to the URI /api/admin/division.
     * @param name The new name of the division.
     * @param oldName The old name of the division (might be equal to new name)
     * @param description The description of the division (may be empty)
     * @param admin The name of the administrating user (may be empty)
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> saveDivision(@RequestParam String name,
                                                             @RequestParam String oldName,
                                                             @RequestParam String description,
                                                             @RequestParam String admin,
                                                             @CurrentUser User currentUser)
    {
        logger.trace("Saving division");
        //String successMessage = "Successfully saved division";
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
                logger.debug("An existing division is changed (" + oldName + "). The name is unchanged.");
            } else if((oldDivision = division = divisionRepository.findByName(oldName)) != null && divisionRepository.findByName(name) == null)
            {
                //An existing divisions name is changed and the name is unique
                logger.debug("An existing division is changed (including its name). The name changed from " + oldName + " to " + name);
            } else if(division.getParent() == null)
            {
                logger.debug("The root division is not allowed to be modified through this API");
                return new ResponseEntity<>("The root division is not allowed to be modified through this API", HttpStatus.FORBIDDEN);
            } else
            {
                logger.warn("Problem finding existing division (" + oldName + "), either the existing division could not be located or the new name is already taken");
                return new ResponseEntity<>("Problem finding existing division, either the existing division could not be located or the new name is already taken", HttpStatus.BAD_REQUEST);
            }

            if(currentUser.isAllowedToAdministrate(division, divisionRepository)) //Check if user is allowed to change the division (if he administrates one of the parent divisions)
            {
                User adminUser = null;
                if (admin != null && !admin.isEmpty())
                {
                    adminUser = userRepository.findByEmail(admin);
                    if (adminUser == null)
                    {
                        logger.warn("Unable to find specified admin user: " + admin);
                        return new ResponseEntity<>("Unable to find specified admin user.", HttpStatus.BAD_REQUEST);
                    }
                } else
                {
                    logger.warn("No admin stated for division " + division.getName());
                }
                division.setAdminUser(adminUser);
                division.setName(name);
                division.setDesc(description);

                try
                {
                    if (oldDivision != null)
                    {
                        logger.debug("Deleting old division " + oldDivision.getName());
                        divisionRepository.delete(oldDivision);
                    }
                    divisionRepository.save(division);
                    logger.info("Successfully saved division " + division.getName());
                    return new ResponseEntity<>("Successfully saved division", HttpStatus.OK);
                } catch (ConstraintViolationException e)
                {
                    logger.warn("A database constraint was violated while saving the division: " + e.getMessage());
                    return new ResponseEntity<>("A database constraint was violated while saving the division.", HttpStatus.BAD_REQUEST);
                }
            } else
            {
                logger.warn("User " + currentUser.getEmail() + " is not allowed to change the division (" + division.getName() + ")");
                return new ResponseEntity<>("You are not allowed to change this division.", HttpStatus.FORBIDDEN);
            }
        } else
        {
            logger.warn("User " + currentUser.getEmail() + " is not allowed to create a new division.");
            return new ResponseEntity<>("You are not allowed to create a new division", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * This function is creating a new division and chooses the name based on the new division name and an integer, depending how many unnamed division exist. This function is invoked, by POSTing to the URI /api/admin/division together with a "new" non-empty parameter.
     * @param newFlag The non-empty parameter indicating the creation of a new empty division.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code together with a JSON map object, containing an 'errorMessage', or a 'successMessage' respectively. If the operation was successful the name of the new division is accessible via 'newDivisionName'.
     */
    @RequestMapping(method = RequestMethod.POST, params = "new", produces = "application/json")
    public @ResponseBody ResponseEntity<Map<String, String>> createDivision(@RequestParam("new") String newFlag, @CurrentUser User currentUser)
    {
        logger.trace("Creating a new empty division");
        Map<String, String> responseMap = new HashMap<>();
        List<Division> administratedDivisions = getOptimizedSetOfAdministratedDivisions(currentUser);
        Division newDivision;
        if(newFlag.isEmpty())
        {
            logger.warn("The new flag is not allowed to be empty");
            responseMap.put("errorMessage", "The new flag parameter is not allowed to be empty");
            return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
        } else if(administratedDivisions != null && administratedDivisions.size() > 0)
        {
            String newName = newDivisionName;
            for(int i = 1; divisionRepository.findByName(newName) != null; i++)
            {
                newName = newDivisionName.concat(" " + i);
            }

            logger.debug("The temporary name of the new division is " + newName);

            newDivision = new Division();
            //If there is a new division the parent is one of the administrated divisions. The correct layout is updated through a different request.
            newDivision.setParent(administratedDivisions.get(0));
            newDivision.setName(newName);
            try
            {
                divisionRepository.save(newDivision);
                logger.info("The new division was successfully created with name " + newName);
                responseMap.put("successMessage", "The new division was successfully created");
                responseMap.put("newDivisionName", newName);
                return new ResponseEntity<>(responseMap, HttpStatus.OK);
            } catch (ConstraintViolationException e)
            {
                logger.warn("A database constraint was violated while saving the division: " + e.getMessage());
                responseMap.put("errorMessage", "A database constraint was violated while saving the division.");
                return new ResponseEntity<>(responseMap, HttpStatus.BAD_REQUEST);
            }
        } else
        {
            logger.warn("User " + currentUser.getEmail() + " is not allowed to create a new division");
            responseMap.put("errorMessage", "You are not allowed to create a new division");
            return new ResponseEntity<>(responseMap, HttpStatus.FORBIDDEN);
        }
    }

    /**
     * This function is deleting the stated division. The function is invoked, by DELETEing the parameter to the URI /api/admin/division
     * @param divisionName The name of the division that needs to be deleted.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public @ResponseBody ResponseEntity<String> deleteDivision(@RequestParam String divisionName, @CurrentUser User currentUser)
    {
        logger.trace("Deleting an existing division");
        Division deletedDivision = divisionRepository.findByName(divisionName);
        if(deletedDivision == null)
        {
            logger.warn("Unable to find stated division " + divisionName);
            return new ResponseEntity<>("Unable to find the stated division", HttpStatus.BAD_REQUEST);
        } else if (!currentUser.isAllowedToAdministrate(deletedDivision, divisionRepository))
        {
            logger.warn("User " + currentUser.getEmail() + " is not allowed to delete the division " + deletedDivision.getName());
            return new ResponseEntity<>("You are not allowed to delete the selected division", HttpStatus.BAD_REQUEST);
        } else
        {
            try
            {
                divisionRepository.delete(deletedDivision);
                logger.info("Successfully deleted division " + divisionName);
                return new ResponseEntity<>("Successfully deleted selected division", HttpStatus.OK);
            } catch (IllegalArgumentException e)
            {
                logger.warn("Unable to delete division " + divisionName);
                return new ResponseEntity<>("Unable to delete the selected division", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * This function gathers the names of all available divisions and returns them. The function is invoked by GETting the URI /api/admin/division
     * @param term A term, that is required to be part of the division name.
     * @return An HTTP response with a status code, together with the JSON list-object of all divisions, or only an error code if an error occurred.
     */
    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<List<Division>> getDivision(@RequestParam(required = false) String term)
    {
        List<Division> divisions;
        if(term == null || term.isEmpty())
        {
            logger.trace("Retrieving all divisions");
            divisions = divisionRepository.findAllNames();
        } else
        {
            logger.trace("Retrieving all divisions using the search term " + term);
            divisions = divisionRepository.findAllNamesContainingString(term);
        }

        if(divisions == null)
        {
            logger.warn("Unable to get divisions" + (term != null? " matching term " + term: ""));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            logger.info("Returning all divisions" + (term != null? " matching term " + term: ""));
            return new ResponseEntity<>(divisions, HttpStatus.OK);
        }
    }

    /**
     * This function returns a single division as JSON object, where the administrator's fields are reduced to his name and email. The function is invoked by GETting the URI /api/admin/division using the parameter name.
     * @param name The name of the division.
     * @return An HTTP response with a status code, together with the JSON object of the divisions, or only an error code if an error occurred.
     */
    @RequestMapping(produces = "application/json", params = "name", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Division> getSingleDivision (@RequestParam String name)
    {
        logger.trace("Getting division " + name);
        Division searchedDivision;
        if(name.isEmpty())
        {
            logger.warn("The name is not allowed to be empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if((searchedDivision = divisionRepository.findByName(name)) == null)
        {
            logger.warn("Unable to find division: " + name);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            if (searchedDivision.getAdminUser() != null)
            {
                logger.debug("Clearing admin user of division " + name);
                searchedDivision.getAdminUser().removeEverythingExceptEmailAndName();
            }
            logger.debug("Returning division " + name);
            return new ResponseEntity<>(searchedDivision, HttpStatus.OK);
        }
    }

    /**
     * This controller is invoked as soon as an administrator is changing the layout of the division tree. The function is invoked, by POSTing the parameters to the URI /api/admin/division/divisionTree
     * @param moved_node The name of the node that has been moved.
     * @param target_node The name of the node, where the moved_node got moved to.
     * @param position The relation the moved_node is positioned to the target_node.
     * @param previous_parent The previous parent of the node.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    @RequestMapping(value="divisionTree", method = RequestMethod.POST)
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
                    logger.debug("Changing layout of division tree");
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
                    logger.debug("Changing layout of division tree");
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
     * This function gathers the complete division tree that the user is administrating. The function is invoked by GETting the URI /api/admin/division/divisionTree.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. In case of success a list of tree nodes, that represent the division tree, are bundled with the status code, otherwise just the error code is returned.
     */
    @RequestMapping(value = "divisionTree", produces = "application/json", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<List<TreeNode>> getDivisionTree(@CurrentUser User currentUser)
    {
        logger.trace("Gathering the division tree");
        List<Division> divisions = getOptimizedSetOfAdministratedDivisions(currentUser);
        if(divisions != null)
        {
            if(!divisions.isEmpty())
            {
                List<TreeNode> divisionTree = new ArrayList<>();
                divisions.parallelStream().forEach(div -> divisionTree.add(getSubTree(div)));
                if(divisionTree.isEmpty())
                {
                    logger.warn("The division tree is empty");
                    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                } else
                {
                    logger.debug("Returning the division tree");
                    return new ResponseEntity<>(divisionTree, HttpStatus.OK);
                }
            } else
            {
                logger.warn("The optimized set of administrated divisions for the user " + currentUser.getEmail() + " is empty");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else
        {
            logger.warn("Unable to find divisions for user " + currentUser.getEmail());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This function moves the selected division (and all subdivisions) to a new parent. The function evaluates if the user is allowed to perform this action.
     * @param selectedDivision The division that needs to be moved.
     * @param newParent The new parent for the division.
     * @param currentUser The currently logged in user.
     * @return An HTTP response with a status code. If an error occurred an error message is bundled into the response, otherwise a success message is available.
     */
    private ResponseEntity<String> changeDivisionParent(Division selectedDivision, Division newParent, User currentUser)
    {
        logger.debug("Changing layout: " + selectedDivision.getName() + " moved to parent " + newParent.getName());

        //The moved node and the target need to be administrated by the user. The nodes should not be root nodes of the user's administration.
        if(currentUser.isAllowedToAdministrate(newParent, divisionRepository) && currentUser.isAllowedToAdministrate(selectedDivision, divisionRepository))
        {
            logger.debug("Changing structure.");
            updateSubtree(selectedDivision, newParent);
            logger.debug("Successfully changed structure");
            return new ResponseEntity<>("Successfully updated division tree", HttpStatus.OK);
        } else
        {
            logger.warn("The user " + currentUser.getEmail() +  " is not allowed to move the node");
            return new ResponseEntity<>("You are not allowed to move the node", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * This recursive parallel executed function updates the complete subtree.
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
     * This recursive function gathers the subtree starting at the selected node.
     * @param divisionRoot The root node of the searched sub tree.
     * @return The sub tree starting at the stated division node.
     */
    public TreeNode getSubTree(Division divisionRoot)
    {
        if(divisionRoot != null)
        {
            logger.trace("Gathering subtree for division " + divisionRoot.getName());
            List<Division> children = divisionRepository.findByParent(divisionRoot);
            TreeNode subTree = new TreeNode(divisionRoot.getName());
            if (children != null && !children.isEmpty())
            {
                children.stream().forEach(division -> subTree.addChildren(getSubTree(division)));
            }
            return subTree;
        } else
        {
            logger.warn("Unable to gather subtree, because division is null");
            return null;
        }
    }

    /**
     * This function is collecting all divisions administrated by the user and only returns the divisions that are closest to the root node on their respective paths.
     * @param currentUser The currently logged in user.
     * @return The optimized set of administrated divisions.
     */
    public List<Division> getOptimizedSetOfAdministratedDivisions(User currentUser)
    {
        if(currentUser == null)
        {
            logger.warn("Trying to gather optimized set of administrated divisions, but user is null");
            return null;
        } else
        {
            logger.trace("Gathering optimized set of administrated divisions.");
            // Checking if user is superadmin, which concludes he would administrate every division.
            return currentUser.isSuperAdmin() ? divisionRepository.findByParent(null) //Returning root node
                    : getOptimizedSetOfDivisions(divisionRepository.findByAdminUser(currentUser)); //Return an optimized set of divisions if he is a normal admin
        }
    }

    /**
     * This function is using a set of divisions, and reduces it to the divisions closest to the root
     * @param unoptimizedSetOfDivisions A set of divisions.
     * @return The list of optimized divisions.
     */
    public static List<Division> getOptimizedSetOfDivisions(List<Division> unoptimizedSetOfDivisions)
    {
        if(unoptimizedSetOfDivisions == null || unoptimizedSetOfDivisions.isEmpty())
        {
            logger.warn("Trying to optimize set of divisions, but unoptimized set is either null or empty");
            return null;
        } else
        {
            logger.debug("Optimizing division set.");
            //Reducing the list to the divisions that are on the top of the tree, removing all unnecessary divisions.
            return unoptimizedSetOfDivisions.stream() //Creating a stream of all divisions
                    .filter(division -> unoptimizedSetOfDivisions.stream() //filtering all divisions that are already defined in a divisions that is closer to the root of the tree
                            .noneMatch(allDivisions -> division.getAncestors().contains(allDivisions))) //Checking, if there is any division in the list, that is an ancestor of the current division. If there is a match there exists a closer division.
                    .collect(Collectors.toList()); // Converting the stream to a list
        }
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
                this.children = new ArrayList<>();
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
