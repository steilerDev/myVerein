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

import de.steilerdev.myVerein.server.model.division.Division;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings
{
    @Id
    private String id;

    @Transient
    private final String systemVersion = "0.1-BETA1";

    private String clubName;

    @DBRef
    private Division rootDivision;

    private List<String> customUserFields;

    private boolean initialSetup;

    @Transient
    private static Logger logger = LoggerFactory.getLogger(Settings.class);


    static public Settings loadSettings(SettingsRepository settingsRepository)
    {
        logger.debug("Getting current settings");
        List<Settings> currentSettings = settingsRepository.findAll();
        Settings currentSetting;

        if(currentSettings == null || currentSettings.isEmpty())
        {
            logger.info("Unable to find settings in database, creating new object");
            currentSetting = new Settings();
            settingsRepository.save(currentSetting);
        } else
        {
            currentSetting = currentSettings.remove(0);
            if(!currentSettings.isEmpty())
            {
                logger.info("There are more settings objects present in database, deleting unnecessary ones");
                settingsRepository.delete(currentSettings);
            }
        }
        return currentSetting;
    }

    public Map<String, Object> getSettingsMap()
    {
        Map<String, Object> settingsMap = new HashMap<>();
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

    public String getSystemVersion()
    {
        return systemVersion;
    }

    public Division getRootDivision()
    {
        return rootDivision;
    }

    public void setRootDivision(Division rootDivision)
    {
        this.rootDivision = rootDivision;
    }

    public boolean isInitialSetup()
    {
        return initialSetup;
    }

    public void setInitialSetup(boolean initialSetup)
    {
        this.initialSetup = initialSetup;
    }
}
