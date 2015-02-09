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
package de.steilerdev.myVerein.server.controller;

import com.mongodb.gridfs.GridFSDBFile;
import de.steilerdev.myVerein.server.model.GridFSRepository;
import de.steilerdev.myVerein.server.model.SettingsRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * This controller is used to server files, stored within the database.
 */
@Controller
@RequestMapping("/content")
public class ContentController
{
    private static Logger logger = LoggerFactory.getLogger(ContentController.class);

    @Autowired
    GridFSRepository gridFSRepository;

    @Autowired
    SettingsRepository settingsRepository;

    /**
     * This function gathers the club logo either from the database or the classpath, depending if the user uploaded a custom logo.
     * @param defaultLogo If this parameter is present the method will return the default logo, without searching through the database.
     * @return The current club logo.
     */
    @RequestMapping(value = "clubLogo", produces = "image/png")
    @ResponseBody ResponseEntity<byte[]> getClubLogo(@RequestParam (required = false) String defaultLogo)
    {
        logger.debug("Loading club logo");
        GridFSDBFile clubLogo = null;
        if(defaultLogo == null)
        {
            //Loading file only from database if the request did not state the fact to use the default logo.
            clubLogo = gridFSRepository.findClubLogo();
        }

        if(clubLogo == null || defaultLogo != null)
        {
            logger.info("No club logo found, using default logo");
            try
            {
                return new ResponseEntity<>(IOUtils.toByteArray(new ClassPathResource("content/Logo.png").getInputStream()), HttpStatus.OK);
            } catch (IOException e)
            {
                logger.warn("Unable to load default logo: " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else
        {
            logger.debug("Loading club logo from database");
            try
            {
                return new ResponseEntity<>(IOUtils.toByteArray(clubLogo.getInputStream()), HttpStatus.OK);
            } catch (IOException e)
            {
                logger.warn("Unable to load club logo from database: " + e.getMessage());
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @RequestMapping("clubName")
    @ResponseBody ResponseEntity<String> getClubName()
    {
        String clubName = settingsRepository.getClubName();
        if(clubName == null || clubName.isEmpty())
        {
            return new ResponseEntity<>("Unable to get club name", HttpStatus.INTERNAL_SERVER_ERROR);
        } else
        {
            return new ResponseEntity<>(clubName, HttpStatus.OK);
        }
    }
}
