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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user/division")
public class DivisionController
{
    private static Logger logger = LoggerFactory.getLogger(DivisionController.class);

    @Autowired
    private DivisionRepository divisionRepository;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Division> getDivision(@RequestParam String divisionID, @CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Loading division with ID " + divisionID);
        Division searchedDivision;
        if(divisionID.isEmpty())
        {
            logger.warn("[" + currentUser + "] The division ID is empty");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if ((searchedDivision = divisionRepository.findById(divisionID)) == null)
        {
            logger.warn("[" + currentUser + "] Unable to find division with the stated ID " + divisionID);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            searchedDivision.prepareForInternalSync();
            logger.info("[" + currentUser + "] Returning division with ID " + divisionID);
            return new ResponseEntity<>(searchedDivision, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "sync", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Division>> syncUserDivision(@CurrentUser User currentUser)
    {
        logger.trace("[" + currentUser + "] Syncing division");
        List<Division> divisions = currentUser.getDivisions();
        if (divisions == null )
        {
            logger.warn("[" + currentUser + "] No divisions found");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            divisions.parallelStream().forEach(Division::removeEverythingExceptId);
            logger.info("[" + currentUser + "] Returning user divisions");
            return new ResponseEntity<>(divisions, HttpStatus.OK);
        }
    }

}
