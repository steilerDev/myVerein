/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package de.steilerdev.myVerein.server.model.division;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.steilerdev.myVerein.server.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains static helper functions needed while handling divisions.
 */
public class DivisionHelper
{
    private final static Logger logger = LoggerFactory.getLogger(DivisionHelper.class);


    //Todo: These two are expensive!! Especially expanded set, which is called fairly often.

    /**
     * This function is using a set of divisions, and reduces it to the divisions closest to the root
     * @param unoptimizedSetOfDivisions A set of divisions.
     * @return The list of optimized divisions.
     */
    @JsonIgnore
    @Transient
    public static List<Division> getOptimizedSetOfDivisions(List<Division> unoptimizedSetOfDivisions)
    {
        if(unoptimizedSetOfDivisions == null || unoptimizedSetOfDivisions.isEmpty())
        {
            logger.warn("Trying to optimize set of divisions, but unoptimized set is either null or empty");
            return null;
        } else if (unoptimizedSetOfDivisions.size() == 1)
        {
            return unoptimizedSetOfDivisions;
        } else
        {
            logger.debug("Optimizing division set");
            //Reducing the list to the divisions that are on the top of the tree, removing all unnecessary divisions.
            return unoptimizedSetOfDivisions.parallelStream() //Creating a stream of all divisions
                    .filter(division -> unoptimizedSetOfDivisions.parallelStream().sorted() //filtering all divisions that are already defined in a divisions that is closer to the root of the tree. Using a parallel and sorted stream, because therefore the likeliness of an early match increases
                            .noneMatch(allDivisions -> division.getAncestors().contains(allDivisions))) //Checking, if there is any division in the list, that is an ancestor of the current division. If there is a match there exists a closer division.
                    .collect(Collectors.toList()); // Converting the stream to a list
        }
    }

    /**
     * This function expands the set of divisions. This means that every division, the user is part of (all child divisions of every division) are going to be returned.
     * @param initialSetOfDivisions The set of divisions that needs to be expanded.
     * @param divisionRepository The division repository, needed to get queried.
     * @return The expanded list of divisions.
     */
    @JsonIgnore
    @Transient
    public static List<Division> getExpandedSetOfDivisions(List<Division> initialSetOfDivisions, DivisionRepository divisionRepository)
    {
        if( (initialSetOfDivisions = getOptimizedSetOfDivisions(initialSetOfDivisions)) == null)
        {
            logger.warn("Trying to expand a set of divisions, but initial set is either null or empty");
            return null;
        } else
        {
            logger.debug("Expanding division set");
            HashSet<Division> expandedSetOfDivisions = new HashSet<>();

            for (Division division: initialSetOfDivisions)
            {
                expandedSetOfDivisions.addAll(divisionRepository.findByAncestors(division));
                expandedSetOfDivisions.add(division);
            }
            return new ArrayList<>(expandedSetOfDivisions);
        }
    }

    /**
     * This function is collecting all divisions administrated by the user and only returns the divisions that are closest to the root node on their respective paths.
     * @param currentUser The currently logged in user.
     * @return The optimized set of administrated divisions.
     */
    public static List<Division> getOptimizedSetOfAdministratedDivisions(User currentUser, DivisionRepository divisionRepository)
    {
        if(currentUser == null)
        {
            logger.warn("Trying to gather optimized set of administrated divisions, but user is null");
            return null;
        } else
        {
            logger.trace("[{}] Gathering optimized set of administrated divisions", currentUser);
            // Checking if user is superadmin, which concludes he would administrate every division.
            return currentUser.isSuperAdmin() ? divisionRepository.findByParent(null) //Returning root node
                    : getOptimizedSetOfDivisions(divisionRepository.findByAdminUser(currentUser)); //Return an optimized set of divisions if he is a normal admin
        }
    }
}
