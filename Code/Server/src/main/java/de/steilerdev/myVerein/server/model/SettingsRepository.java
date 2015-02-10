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
package de.steilerdev.myVerein.server.model;

import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * This class is used for reading and writing the properties file containing the system settings.
 */
public class SettingsRepository
{
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SimpleMongoDbFactory mongoDbFactory;

    @Autowired
    private ApplicationContext applicationContext;

    private Properties settings;
    private Resource settingsResource;

    private final static String settingsFileName = "myVerein.properties";
    //Property names
    private final static String databaseName = "dbName";
    private final static String databaseHost = "dbHost";
    private final static String databasePort = "dbPort";
    private final static String databaseUser = "dbUser";
    private final static String databasePassword = "dbPassword";
    private final static String rememberMeKey = "rememberMeKey";
    private final static String clubName = "clubName";
    private final static String initSetup = "initSetup";

    private boolean changed;
    private boolean databaseChanged;

    private static Logger logger = LoggerFactory.getLogger(SettingsRepository.class);

    public Properties loadSettings() throws IOException
    {
        if(settings == null)
        {
            logger.debug("Loading settings file from classpath");
            settingsResource = new ClassPathResource(settingsFileName);
            changed = false;
            databaseChanged = false;
            return (settings = PropertiesLoaderUtils.loadProperties(settingsResource));
        } else
        {
            return settings;
        }
    }

    /**
     * Internally sets the database host. Note you first have to call save settings, before they are permanently stored.
     * @param newDatabaseHost The new database host.
     * @throws IOException If an error during property loading occurs.
     */
    public void setDatabaseHost(String newDatabaseHost) throws IOException
    {
        if(newDatabaseHost != null && !newDatabaseHost.equals(getDatabaseHost()))
        {
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databaseHost, newDatabaseHost);
        }
    }

    public String getDatabaseHost()
    {
        try
        {
            return loadSettings().getProperty(databaseHost);
        } catch (IOException e)
        {
            logger.debug("Unable to load database host");
            return null;
        }
    }

    /**
     * Internally sets the database port. Note you first have to call save settings, before they are permanently stored.
     * @param newDatabasePort The new database port.
     * @throws IOException If an error during property loading occurs.
     */
    public void setDatabasePort(String newDatabasePort) throws IOException
    {
        if(newDatabasePort != null && !newDatabasePort.equals(getDatabasePort()))
        {
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databasePort, newDatabasePort);
        }
    }

    public String getDatabasePort()
    {
        try
        {
            return loadSettings().getProperty(databasePort);
        } catch (IOException e)
        {
            logger.debug("Unable to load database port");
            return null;
        }
    }

    /**
     * Internally sets the database name. Note you first have to call save settings, before they are permanently stored.
     * @param newDatabaseName The new database name.
     * @throws IOException If an error during property loading occurs.
     */
    public void setDatabaseName(String newDatabaseName) throws IOException
    {
        if(newDatabaseName != null && !newDatabaseName.equals(getDatabaseName()))
        {
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databaseName, newDatabaseName);
        }
    }

    public String getDatabaseName()
    {
        try
        {
            return loadSettings().getProperty(databaseName);
        } catch (IOException e)
        {
            logger.debug("Unable to load database name");
            return null;
        }
    }

    /**
     * Internally sets the database user. Note you first have to call save settings, before they are permanently stored.
     * @param newDatabaseUser The new database user.
     * @throws IOException If an error during property loading occurs.
     */
    public void setDatabaseUser(String newDatabaseUser) throws IOException
    {
        if(newDatabaseUser != null && !newDatabaseUser.equals(getDatabaseUser()))
        {
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databaseUser, newDatabaseUser);
        }
    }

    public String getDatabaseUser()
    {
        try
        {
            return loadSettings().getProperty(databaseUser);
        } catch (IOException e)
        {
            logger.debug("Unable to load database user");
            return null;
        }
    }

    /**
     * Internally sets the database password. Note you first have to call save settings, before they are permanently stored.
     * @param newDatabasePassword The new database password.
     * @throws IOException If an error during property loading occurs.
     */
    public void setDatabasePassword(String newDatabasePassword) throws IOException
    {
        if(newDatabasePassword != null && !newDatabasePassword.equals(getDatabasePassword()))
        {
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databasePassword, newDatabasePassword);
        }
    }

    public String getDatabasePassword()
    {
        try
        {
            return loadSettings().getProperty(databasePassword);
        } catch (IOException e)
        {
            logger.debug("Unable to load database password");
            return null;
        }
    }

    /**
     * Internally sets the remember me key. Note you first have to call save settings, before they are permanently stored.
     * @param newRememberMeKey The new remember me key.
     * @throws IOException If an error during property loading occurs.
     */
    public void setRememberMeKey(String newRememberMeKey) throws IOException
    {
        if(newRememberMeKey != null && !newRememberMeKey.equals(getRememberMeKey()))
        {
            changed = true;
            loadSettings().setProperty(rememberMeKey, newRememberMeKey);
        }
    }

    public String getRememberMeKey()
    {
        try
        {
            return loadSettings().getProperty(rememberMeKey);
        } catch (IOException e)
        {
            logger.debug("Unable to load remember me key");
            return null;
        }
    }

    /**
     * Internally sets the club name. Note you first have to call save settings, before they are permanently stored.
     * @param newClubName The new club name.
     * @throws IOException If an error during property loading occurs.
     */
    public void setClubName(String newClubName) throws IOException
    {
        if(newClubName != null && !newClubName.equals(getClubName()))
        {
            changed = true;
            loadSettings().setProperty(clubName, newClubName);
        }
    }

    public String getClubName()
    {
        try
        {
            return loadSettings().getProperty(clubName);
        } catch (IOException e)
        {
            logger.debug("Unable to load club name");
            return null;
        }
    }

    /**
     * This function checks if the init setup flag is set within the settings file. If the flag is available, but empty or true, the function is returning true, otherwise false.
     * @return
     */
    public boolean isInitSetup()
    {
        logger.trace("Checking if initial setup needs to be run.");
        try
        {
            String initSetupString = loadSettings().getProperty(initSetup);
            return initSetupString != null && (initSetupString.isEmpty() || initSetupString.trim().equals("true"));
        } catch (IOException e)
        {
            logger.debug("Unable to load init setup flag");
            return false;
        }
    }

    public void setInitSetup(boolean initSetupFlag) throws IOException
    {
        boolean oldInitSetupFlag = isInitSetup();
        if(!((initSetupFlag && oldInitSetupFlag) || (!initSetupFlag && !oldInitSetupFlag)))
        {
            changed = true;
            loadSettings().setProperty(initSetup, initSetupFlag? "true": "false");
        }
    }

    public void saveSettings(User currentUser) throws IOException
    {
        saveSettings(loadSettings(), currentUser);
    }

    public void saveSettings(Properties settings, User currentUser) throws IOException, BeansException, IllegalStateException, MongoException
    {
        if(changed)
        {
            logger.debug("Saving settings to " + settingsResource.getFile().getAbsolutePath());
            settings.store(new FileOutputStream(settingsResource.getFile()), "Settings last changed " + (currentUser != null ? ("by " + currentUser.getEmail() + " (" + LocalDateTime.now().toString() + ")") : LocalDateTime.now().toString()));
            if(databaseChanged)
            {
                logger.info("Restarting application context, because database configuration changed");

                try
                {
                    //Getting servlet context to be able to re initiate it.
                    ServletContext servletContext = ((XmlWebApplicationContext) applicationContext).getServletContext();

                    //Closing application context, mongoDB and servlet context.
                    mongoDbFactory.destroy();
                    ContextLoader contextLoader = new ContextLoader();
                    contextLoader.closeWebApplicationContext(servletContext);

                    //Restarting servlet context
                    contextLoader.initWebApplicationContext(servletContext);
                } catch (Exception e)
                {
                    throw new MongoException(e.getMessage());
                }
            }
            this.settings = null;
        } else
        {
            logger.debug("No need to save the settings");
        }
    }
}
