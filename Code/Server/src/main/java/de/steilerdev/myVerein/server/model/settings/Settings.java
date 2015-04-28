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
package de.steilerdev.myVerein.server.model.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.steilerdev.myVerein.server.model.BaseEntity;
import de.steilerdev.myVerein.server.model.division.Division;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends BaseEntity
{

    @Transient
    private final String systemVersion = "0.1-BETA1";

    @DBRef
    private Division rootDivision;
    private String clubName;
    private List<String> customUserFields;
    private boolean initialSetup;

    /*
        Mandatory basic getter and setter
     */

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

    /*
        Convenience getter and setter
     */

    @Transient
    @JsonIgnore
    public Map<String, Object> getSettingsMap()
    {
        Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put("clubName", clubName);
        settingsMap.put("customUserFields", customUserFields);
        return settingsMap;
    }

    /*
        Required java object functions
     */

    @Override
    public int hashCode()
    {
        return id == null? 0: id.hashCode();
    }

    /**
     * Equality is based on the same id String within the database.
     * @param obj The object compared to the current object.
     * @return True if the IDs of the two objects
     */
    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof Settings && this.id != null && this.id.equals(((Settings) obj).getId());
    }

    @Override
    public String toString()
    {
        return clubName != null && !clubName.isEmpty()? "Settings for " + clubName + " system version " + systemVersion: id;
    }
}
