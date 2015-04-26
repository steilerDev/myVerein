/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
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
package de.steilerdev.myVerein.server.controller.user;


import de.steilerdev.myVerein.server.model.division.Division;
import de.steilerdev.myVerein.server.model.division.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * This class handles gathering information about divisions by a user.
 */
@RestController
@RequestMapping("/api/user/division")
public class DivisionController
{
    private final Logger logger = LoggerFactory.getLogger(DivisionController.class);

    @Autowired
    private DivisionRepository divisionRepository;

    /**
     * This function retrieves and returns the division identified by its id. The function is invoked by GETting the URI /api/user/division with the id parameter.
     * @param divisionID The division id of the searched division.
     * @param currentUser The currently logged in user.
     * @return A response entity containing a division with all appropriate information together with a success code. If an error occurred an empty response entity with a failure code is returned.
     */
    @RequestMapping(method = RequestMethod.GET, params = "id",produces = "application/json")
    public ResponseEntity<Division> getDivision(@RequestParam(value = "id") String divisionID, @CurrentUser User currentUser)
    {
        logger.trace("[{}] Loading division with ID {}", currentUser, divisionID);
        Division searchedDivision;
        if(divisionID.isEmpty())
        {
            logger.warn("[{}] The division ID is empty", currentUser);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if ((searchedDivision = divisionRepository.findById(divisionID)) == null)
        {
            logger.warn("[{}] Unable to find division with the stated ID {}", currentUser, divisionID);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else
        {
            logger.info("{}] Returning division {}", currentUser, searchedDivision);
            return new ResponseEntity<>(searchedDivision.getSendingObjectInternalSync(), HttpStatus.OK);
        }
    }

    /**
     * This function gathers and returns all division, the user is part of. The function is invoked by GETting the URI /api/user/division.
     * @param currentUser The currently logged in user.
     * @return A response entity with a list of divisions, reduced to their ids together with a success code, if the execution was successful, otherwise an empty response entity with an error code is returned.
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Division>> getUserDivision(@CurrentUser User currentUser)
    {
        logger.trace("[{}] Syncing division", currentUser);
        List<Division> divisions = currentUser.getDivisions();
        if (divisions == null)
        {
            logger.warn("[{}] No divisions found", currentUser);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            divisions.replaceAll(Division::getSendingObjectOnlyId);
            logger.info("[{}] Returning user divisions", currentUser);
            return new ResponseEntity<>(divisions, HttpStatus.OK);
        }
    }

}
