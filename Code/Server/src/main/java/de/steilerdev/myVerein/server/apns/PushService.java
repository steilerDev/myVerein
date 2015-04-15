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


import com.relayrides.pushy.apns.ApnsEnvironment;
import com.relayrides.pushy.apns.PushManager;
import com.relayrides.pushy.apns.PushManagerConfiguration;
import com.relayrides.pushy.apns.util.SSLContextUtil;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PushService
{
    private static Logger logger = LoggerFactory.getLogger(PushService.class);

    private static PushManager<SimpleApnsPushNotification> pushManager;
    private final static String apnsSettingsFile = "apns/apns.properties";
    private final static String fileKey = "file";
    private final static String passwordKey = "password";

    /**
     * This function creates a new APNS instance. If the function returns null, APNS is not supported.
     * @return The current APNS instance. If null APNS is not supported by this server.
     */
    public static PushManager<SimpleApnsPushNotification> getInstanced()
    {
        if(pushManager == null)
        {
            logger.debug("Creating new APNS instance");
            try
            {
                Resource settingsResource = new ClassPathResource(apnsSettingsFile);
                Properties settings = PropertiesLoaderUtils.loadProperties(settingsResource);

                InputStream certFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(settings.getProperty(fileKey));

                 pushManager = new PushManager<>(
                                ApnsEnvironment.getSandboxEnvironment(),
                                SSLContextUtil.createDefaultSSLContext(certFile, settings.getProperty(passwordKey)),
                                null, // Optional: custom event loop group
                                null, // Optional: custom ExecutorService for calling listeners
                                null, // Optional: custom BlockingQueue implementation
                                new PushManagerConfiguration(),
                                "ExamplePushManager");

                logger.debug("Created new APNS instance, starting");
                //pushManager.start();
            } catch (IOException e)
            {
                logger.warn("Unable to load APNS settings file: {}", e.getMessage());
                return null;
            }catch (Exception e)
            {
                logger.warn("An unknown error occurred: {}", e.getMessage());
            }
        }
        logger.info("Returning APNS instance");
        return pushManager;
    }

    public void sendNotification()
    {
        
    }
}
