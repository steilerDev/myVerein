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

import org.springframework.data.annotation.Id;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.Date;

/**
 * A re-implementation of the PersistentRememberMeToken. Original object is not usable with SpringData MongoDB, because it got no default constructor and fitting setter.
 * This class actually overrides everything and tries to find workarounds for equals and get hash code, to replicate the original implementation as good as possible.
 */
public class RememberMeToken
{
    @Id
    private String id;

    private String username;
    private String series;
    private String tokenValue;
    private Date date;

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

    public PersistentRememberMeToken toPersistentRememberMeToken()
    {
        return new PersistentRememberMeToken(username, series, tokenValue, date);
    }
}
