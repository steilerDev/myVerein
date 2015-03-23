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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import javax.validation.constraints.NotNull;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Settings
{
    @Id
    private String id;

    @Transient
    private final String systemVersion = "0.1-BETA1";

    private String clubName;

    private String rememberMeKey;

    private List<String> customUserFields;

    @Transient
    private static Logger logger = LoggerFactory.getLogger(Settings.class);
    @Transient
    private Properties settings;
    @Transient
    private Resource settingsResource;
    @Transient
    private final static String settingsFileName = "myVerein.properties";
    @Transient
    private boolean databaseChanged;

    //Property names
    @Transient
    private final static String databaseName = "dbName";
    @Transient
    private final static String databaseHost = "dbHost";
    @Transient
    private final static String databasePort = "dbPort";
    @Transient
    private final static String databaseUser = "dbUser";
    @Transient
    private final static String databasePassword = "dbPassword";
    @Transient
    private final static String initSetup = "initSetup";

    public Settings()
    {

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
            databaseChanged = true;
            loadSettingsFromFile().setProperty(databaseHost, newDatabaseHost);
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
            return loadSettingsFromFile().getProperty(databaseHost);
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
            databaseChanged = true;
            loadSettingsFromFile().setProperty(databasePort, newDatabasePort);
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
            return loadSettingsFromFile().getProperty(databasePort);
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
            databaseChanged = true;
            loadSettingsFromFile().setProperty(databaseName, newDatabaseName);
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
            return loadSettingsFromFile().getProperty(databaseName);
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
            databaseChanged = true;
            loadSettingsFromFile().setProperty(databaseUser, newDatabaseUser);
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
            return loadSettingsFromFile().getProperty(databaseUser);
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
            databaseChanged = true;
            loadSettingsFromFile().setProperty(databasePassword, newDatabasePassword);
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
            return loadSettingsFromFile().getProperty(databasePassword);
        } catch (IOException e)
        {
            logger.debug("Unable to load database password");
            return null;
        }
    }

    /**
     * This function checks if the init setup flag is set within the settings file.
     * @return True if the init setup flag is set (empty of true), false otherwise.
     */
    public boolean isInitialSetup()
    {
        logger.trace("Checking if initial setup needs to be run.");
        try
        {
            String initSetupString = loadSettingsFromFile().getProperty(initSetup);
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
    public void setInitialSetup(boolean initSetupFlag)
    {
        logger.trace("Changing initial setup flag");
        try
        {
            boolean oldInitSetupFlag = isInitialSetup();
            if (!((initSetupFlag && oldInitSetupFlag) || (!initSetupFlag && !oldInitSetupFlag)))
            {
                logger.debug("Init setup flag changed, storing new name temporarily");
                loadSettingsFromFile().setProperty(initSetup, initSetupFlag ? "true" : "false");
            }
        } catch (IOException e)
        {
            logger.error("Unable to change initial setup flag: " + e.getMessage());
        }
    }


    /**
     * This function stores the currently loaded settings file, if it did not change. If the database details changed, the method is restarting the context, unfortunately this is not sufficient.
     * @param currentUser The currently logged in user, used to log the person who changed the settings.
     * @return Returns true if successfully saved settings, false otherwise.
     */
    public boolean saveSettings(User currentUser, SettingsRepository settingsRepository)
    {
        if(databaseChanged)
        {
            try
            {
                logger.debug("Saving settings to " + settingsResource.getFile().getAbsolutePath());
                loadSettingsFromFile().store(new FileOutputStream(settingsResource.getFile()), "Settings last changed " + (currentUser != null ? ("by " + currentUser.getEmail() + " (" + LocalDateTime.now().toString() + ")") : LocalDateTime.now().toString()));
                logger.info("You need to restart the application context, because database configuration changed");
                //            try
                //            {
                //                //Getting servlet context to be able to re initiate it.
                //                ServletContext servletContext = ((XmlWebApplicationContext) applicationContext).getServletContext();
                //
                //                //Closing application context, mongoDB and servlet context.
                //                mongoDbFactory.destroy();
                //                ContextLoader contextLoader = new ContextLoader();
                //                contextLoader.closeWebApplicationContext(servletContext);
                //
                //                //Restarting servlet context
                //                contextLoader.initWebApplicationContext(servletContext);
                //            } catch (BeansException | IllegalStateException e)
                //            {
                //                throw e;
                //            } catch (Exception e)
                //            {
                //                throw new MongoException(e.getMessage());
                //            }
                this.settings = null;
            } catch (IOException e)
            {
                logger.error("Unable to save settings: " + e.getMessage());
                return false;
            }
        } else
        {
            logger.debug("No need to save the settings to file");
        }

        if(settingsRepository != null)
        {
            logger.debug("Saving settings to database");
            settingsRepository.save(this);
        } else {
            logger.warn("Not saving settings to database, because there is no repository available");
        }

        return true;
    }

    /**
     * This class checks if the property file is cached, if not it is reloading the file.
     * @return The properties object representation of the current system's property file.
     * @throws java.io.IOException If the properties file could not be loaded.
     */
    private Properties loadSettingsFromFile() throws IOException
    {
        if(settings == null)
        {
            logger.trace("(Re-)Loading settings file from classpath");
            settingsResource = new ClassPathResource(settingsFileName);
            databaseChanged = false;
            return (settings = PropertiesLoaderUtils.loadProperties(settingsResource));
        } else
        {
            logger.debug("Returning cached settings information");
            return settings;
        }
    }

    static public Settings loadSettings(SettingsRepository settingsRepository)
    {
        logger.debug("Getting current settings");
        List<Settings> currentSettings = settingsRepository.findAll();
        Settings currentSetting;

        if(currentSettings == null || currentSettings.isEmpty())
        {
            logger.info("Unable to find settings in database, creating new object");
            currentSetting = new Settings();
            currentSetting.saveSettings(null, settingsRepository);
        } else
        {
            currentSetting = currentSettings.remove(0);
            if(!currentSettings.isEmpty())
            {
                settingsRepository.delete(currentSettings);
            }
        }

        try
        {
            currentSetting.loadSettingsFromFile();
        } catch (IOException e)
        {
            logger.error("Unable to load settings");
        }

        return currentSetting;
    }

    public Map<String, Object> getSettingsMap()
    {
        Map<String, Object> settingsMap;
        try
        {
            settingsMap = (Map<String, Object>)loadSettingsFromFile().clone();
            if(settingsMap == null)
            {
                throw new IOException("There is no settings map");
            }
        } catch (IOException e)
        {
            logger.error("Unable to load settings from file");
            settingsMap = new HashMap<>();
        }
        settingsMap.put("rememberMeKey", rememberMeKey);
        settingsMap.put("clubName", clubName);
        settingsMap.put("customUserFields", customUserFields);

        return settingsMap;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getClubName()
    {
        return clubName;
    }

    public void setClubName(String clubName)
    {
        this.clubName = clubName;
    }

    public List<String> getCustomUserFields()
    {
        return customUserFields;
    }

    public void setCustomUserFields(List<String> customUserFields)
    {
        this.customUserFields = customUserFields;
    }

    public String getRememberMeKey()
    {
        return rememberMeKey;
    }

    public void setRememberMeKey(String rememberMeKey)
    {
        this.rememberMeKey = rememberMeKey;
    }

    public String getSystemVersion()
    {
        return systemVersion;
    }
}
