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
package de.steilerdev.myVerein.server.model.rememberMeToken;

import de.steilerdev.myVerein.server.model.BaseEntity;
import org.springframework.data.annotation.Id;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.Date;

/**
 * A re-implementation of the PersistentRememberMeToken. Original object is not usable with SpringData MongoDB, because it got no default constructor and fitting setter.
 * This class actually overrides everything and tries to find workarounds for equals and get hash code, to replicate the original implementation as good as possible.
 */
public class RememberMeToken extends BaseEntity
{
    private String username;
    private String series;
    private String tokenValue;
    private Date date;

    /*
        Constructors (Empty one to meet bean definition and convenience ones)
     */

    public RememberMeToken() {}

    public RememberMeToken(String username, String series, String tokenValue, Date date)
    {
        this.username = username;
        this.series = series;
        this.tokenValue = tokenValue;
        this.date = date;
    }

    public RememberMeToken(PersistentRememberMeToken token)
    {
        this.username = token.getUsername();
        this.series = token.getSeries();
        this.tokenValue = token.getTokenValue();
        this.date = token.getDate();
    }

    /*
        Mandatory basic getter and setter
     */

    public String getUsername()
    {
        return username;
    }

    public String getSeries()
    {
        return series;
    }

    public String getTokenValue()
    {
        return tokenValue;
    }

    public Date getDate()
    {
        return date;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setSeries(String series)
    {
        this.series = series;
    }

    public void setTokenValue(String tokenValue)
    {
        this.tokenValue = tokenValue;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /*
        Convenience getter and setter
     */

    /**
     * This function converts this object to a persistent remember me token, that is usable in the context of Spring's remember me service.
     * @return A persistent remember me token, similar to this object.
     */
    public PersistentRememberMeToken toPersistentRememberMeToken()
    {
        return new PersistentRememberMeToken(username, series, tokenValue, date);
    }

    /*
        Required java object functions
     */

    @Override
    public int hashCode()
    {
        return id == null? 0: id.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof RememberMeToken && this.id != null && this.id.equals(((RememberMeToken) obj).getId());
    }

    @Override
    public String toString()
    {
        if(username != null && !username.isEmpty() && tokenValue != null && !tokenValue.isEmpty() && series != null && !series.isEmpty())
        {
            return "Remember-me token for " + username + " with value " + tokenValue + " from series " + series;
        } else if (username != null && !username.isEmpty())
        {
            return "Remember-me token for " + username;
        } else
        {
            return id;
        }
    }
}
