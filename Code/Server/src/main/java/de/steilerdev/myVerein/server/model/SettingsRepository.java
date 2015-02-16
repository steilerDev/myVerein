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
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This class is used for reading and writing the properties file containing the system settings. The class also tries to reload all properties on change. Unfortunately this is not working completely yet.
 */
public class SettingsRepository
{
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
    private final static String customUserFields = "customUserFields";
    //The seperator used on the customer user field
    private final static String customUserFieldsSeperator = ",";

    private boolean changed;
    private boolean databaseChanged;

    private static Logger logger = LoggerFactory.getLogger(SettingsRepository.class);

    /**
     * This class checks if the property file is cached, if not it is reloading the file.
     * @return The properties object representation of the current system's property file.
     * @throws IOException If the properties file could not be loaded.
     */
    public Properties loadSettings() throws IOException
    {
        if(settings == null)
        {
            logger.trace("(Re-)Loading settings file from classpath");
            settingsResource = new ClassPathResource(settingsFileName);
            changed = false;
            databaseChanged = false;
            return (settings = PropertiesLoaderUtils.loadProperties(settingsResource));
        } else
        {
            logger.debug("Returning cached settings information");
            return settings;
        }
    }

    /**
     * Internally sets the database host. Note you first have to call save settings, before they are permanently stored.
     * @param newDatabaseHost The new database host.
     * @throws IOException If the properties file could not be loaded.
     */
    public void setDatabaseHost(String newDatabaseHost) throws IOException
    {
        if(newDatabaseHost != null && !newDatabaseHost.equals(getDatabaseHost()))
        {
            logger.debug("Database host changed, storing new host temporarily");
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databaseHost, newDatabaseHost);
        }
    }

    /**
     * Reads the database host property from the system's property file.
     * @return The current database host or null if the properties file could not be loaded.
     */
    public String getDatabaseHost()
    {
        try
        {
            logger.trace("Reading properties file, to retrieve database host property");
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
     * @throws IOException If the properties file could not be loaded.
     */
    public void setDatabasePort(String newDatabasePort) throws IOException
    {
        if(newDatabasePort != null && !newDatabasePort.equals(getDatabasePort()))
        {
            logger.debug("Database port changed, storing new port temporarily");
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databasePort, newDatabasePort);
        }
    }

    /**
     * Reads the database port property from the system's property file.
     * @return The current database port or null if the properties file could not be loaded.
     */
    public String getDatabasePort()
    {
        try
        {
            logger.trace("Reading properties file, to retrieve database port property");
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
     * @throws IOException If the properties file could not be loaded.
     */
    public void setDatabaseName(String newDatabaseName) throws IOException
    {
        if(newDatabaseName != null && !newDatabaseName.equals(getDatabaseName()))
        {
            logger.debug("Database name changed, storing new name temporarily");
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databaseName, newDatabaseName);
        }
    }

    /**
     * Reads the database name property from the system's property file.
     * @return The current database name or null if the properties file could not be loaded.
     */
    public String getDatabaseName()
    {
        try
        {
            logger.trace("Reading properties file, to retrieve database name property");
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
     * @throws IOException If the properties file could not be loaded.
     */
    public void setDatabaseUser(String newDatabaseUser) throws IOException
    {
        if(newDatabaseUser != null && !newDatabaseUser.equals(getDatabaseUser()))
        {
            logger.debug("Database user changed, storing new user temporarily");
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databaseUser, newDatabaseUser);
        }
    }

    /**
     * Reads the database user property from the system's property file.
     * @return The current database user or null if the properties file could not be loaded.
     */
    public String getDatabaseUser()
    {
        try
        {
            logger.trace("Reading properties file, to retrieve database user property");
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
     * @throws IOException If the properties file could not be loaded.
     */
    public void setDatabasePassword(String newDatabasePassword) throws IOException
    {
        if(newDatabasePassword != null && !newDatabasePassword.equals(getDatabasePassword()))
        {
            logger.debug("Database password changed, storing new password temporarily");
            changed = true;
            databaseChanged = true;
            loadSettings().setProperty(databasePassword, newDatabasePassword);
        }
    }

    /**
     * Reads the database password property from the system's property file.
     * @return The current database password or null if the properties file could not be loaded.
     */
    public String getDatabasePassword()
    {
        try
        {
            logger.trace("Reading properties file, to retrieve database password property");
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
     * @throws IOException If the properties file could not be loaded.
     */
    public void setRememberMeKey(String newRememberMeKey) throws IOException
    {
        if(newRememberMeKey != null && !newRememberMeKey.equals(getRememberMeKey()))
        {
            logger.debug("Remember-me-key changed, storing new key temporarily");
            changed = true;
            loadSettings().setProperty(rememberMeKey, newRememberMeKey);
        }
    }

    /**
     * Reads the remember-me-key property from the system's property file.
     * @return The current remember-me-key or null if the properties file could not be loaded.
     */
    public String getRememberMeKey()
    {
        try
        {
            logger.trace("Reading properties file, to retrieve remember-me-key property");
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
     * @throws IOException If the properties file could not be loaded.
     */
    public void setClubName(String newClubName) throws IOException
    {
        if(newClubName != null && !newClubName.equals(getClubName()))
        {
            logger.debug("Club name changed, storing new name temporarily");
            changed = true;
            loadSettings().setProperty(clubName, newClubName);
        }
    }

    /**
     * Reads the club name property from the system's property file.
     * @return The current club name or null if the properties file could not be loaded.
     */
    public String getClubName()
    {
        try
        {
            logger.trace("Reading properties file, to retrieve club name property");
            return loadSettings().getProperty(clubName);
        } catch (IOException e)
        {
            logger.debug("Unable to load club name");
            return null;
        }
    }

    /**
     * This function checks if the init setup flag is set within the settings file.
     * @return True if the init setup flag is set (empty of true), false otherwise.
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

    /**
     * Internally sets the init setup flag. Note you first have to call save settings, before they are permanently stored.
     * @param initSetupFlag The new init setup flag.
     * @throws IOException If the properties file could not be loaded.
     */
    public void setInitSetup(boolean initSetupFlag) throws IOException
    {
        boolean oldInitSetupFlag = isInitSetup();
        if(!((initSetupFlag && oldInitSetupFlag) || (!initSetupFlag && !oldInitSetupFlag)))
        {
            logger.debug("Init setup flag changed, storing new name temporarily");
            changed = true;
            loadSettings().setProperty(initSetup, initSetupFlag? "true": "false");
        }
    }

    /**
     * The function reads the comma separated list of custom user fields and puts them into a list of Strings.
     * @return The list of custom user fields, an empty List if there aren't any fields, or null if the if the properties file could not be loaded.
     */
    public List<String> getCustomUserFields()
    {
        logger.trace("Reading and converting custom user fields");
        try
        {
            String customUserFieldString = loadSettings().getProperty(customUserFields);
            if(customUserFieldString == null)
            {
                logger.warn("Unable to load custom user fields, because they are null");
                return null;
            } else if(customUserFieldString.isEmpty())
            {
                logger.debug("Custom user fields are empty, returning empty list");
                return new ArrayList<>();
            } else
            {
                logger.info("Custom user fields are non-empty, returning appropriate list");
                return Arrays.asList(customUserFieldString.split(customUserFieldsSeperator));
            }
        } catch (IOException e)
        {
            logger.debug("Unable to load custom user fields, because an IOException occurred: " + e.getMessage());
            return null;
        }
    }

    /**
     * Internally sets the custom user fields list. The list is converted to a comma separated String representation. Note you first have to call save settings, before they are permanently stored.
     * @param customUserFieldsList The new custom user fields list.
     * @throws IOException If the properties file could not be loaded.
     */
    public void setCustomUserFields(List<String> customUserFieldsList) throws IOException
    {
        String customUserFieldsString;
        if(customUserFieldsList == null || customUserFieldsList.isEmpty())
        {
            customUserFieldsString = "";
        } else if(customUserFieldsList.parallelStream().anyMatch(entry -> entry.contains(customUserFieldsSeperator)))
        {
            throw new IOException("Unable to save custom user fields, because at least one entry contains a non-allowed char (" + customUserFieldsSeperator + ")");
        } else
        {
            //Only allowing distinct and trimmed keys
            customUserFieldsString = customUserFieldsList.parallelStream().distinct().map(entry -> entry.trim()).collect(Collectors.joining(customUserFieldsSeperator));
        }

        if (!customUserFieldsString.equals(loadSettings().getProperty(customUserFields)))
        {
            changed = true;
            loadSettings().setProperty(customUserFields, customUserFieldsString);
        }
    }

    /**
     * This function stores the currently loaded settings file, if it did not change. If the database details changed, the method is restarting the context, unfortunately this is not sufficient.
     * @param currentUser The currently logged in user, used to log the person who changed the settings.
     * @throws IOException If the property file could not be loaded or written.
     * @throws BeansException If an error occurred during the application context restart.
     * @throws IllegalStateException If an error occurred during the application context restart.
     * @throws MongoException If an error occurred during the application context restart.
     */
    public void saveSettings(User currentUser) throws IOException, BeansException, IllegalStateException, MongoException
    {
        if(changed)
        {
            logger.debug("Saving settings to " + settingsResource.getFile().getAbsolutePath());
            loadSettings().store(new FileOutputStream(settingsResource.getFile()), "Settings last changed " + (currentUser != null ? ("by " + currentUser.getEmail() + " (" + LocalDateTime.now().toString() + ")") : LocalDateTime.now().toString()));
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
                } catch (BeansException | IllegalStateException e)
                {
                    throw e;
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
