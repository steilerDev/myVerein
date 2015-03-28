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
package de.steilerdev.myVerein.server.apns;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

public class PushService
{
    private static Logger logger = LoggerFactory.getLogger(PushService.class);

    private static ApnsService service;
    private static String apnsSettingsFile = "apns/apns.properties";
    private static String fileKey = "file";
    private static String passwordKey = "password";

    /**
     * This function creates a new APNS instance. If the function returns null, APNS is not supported.
     * @return The current APNS instance. If null APNS is not supported by this server.
     */
    public static ApnsService getInstance()
    {
        if(service == null)
        {
            logger.debug("Creating new APNS instance");
            try
            {
                Resource settingsResource = new ClassPathResource(apnsSettingsFile);
                Properties settings = PropertiesLoaderUtils.loadProperties(settingsResource);

                service = APNS.newService()
                        .withCert(settings.getProperty(fileKey), settings.getProperty(passwordKey))
                        .withSandboxDestination()
                        .build();

            } catch (IOException e)
            {
                logger.warn("Unable to load APNS settings file");
                return null;
            }
        }
        logger.info("Returning APNS instance");
        return service;
    }

    public void sendNotification()
    {
        
    }
}
