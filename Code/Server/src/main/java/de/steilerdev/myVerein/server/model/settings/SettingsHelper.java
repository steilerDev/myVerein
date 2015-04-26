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
package de.steilerdev.myVerein.server.model.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This class contains static helper functions needed while handling settings.
 */
public class SettingsHelper
{
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    /**
     * This function uses the provided settings repository to load the appropriate settings file from the database.
     * @param settingsRepository The settings repository, used to query the database.
     * @return The currently active settings document.
     */
    static public Settings loadSettings(SettingsRepository settingsRepository)
    {
        logger.debug("Getting current settings");
        List<Settings> currentSettings = settingsRepository.findAll();

        if(currentSettings == null || currentSettings.isEmpty())
        {
            logger.warn("No settings document available");
            return null;
        } else
        {
            Settings currentSetting = currentSettings.remove(0);
            if (!currentSettings.isEmpty())
            {
                logger.info("There are more settings objects present in database, deleting unnecessary ones");
                settingsRepository.delete(currentSettings);
            }
            return currentSetting;
        }
    }

    /**
     * This function checks if an initial setup for the system is needed. This is the case if either the {@link de.steilerdev.myVerein.server.model.settings.Settings#initialSetup initialSetup} flag is set or there is no settings document available.
     * @param settingsRepository The settings repository needed to query the database.
     * @return True is an initial setup is needed, false otherwise.
     */
    static public boolean initialSetupNeeded(SettingsRepository settingsRepository)
    {
        Settings currentSettings;
        return (currentSettings = SettingsHelper.loadSettings(settingsRepository)) == null || currentSettings.isInitialSetup();
    }
}
