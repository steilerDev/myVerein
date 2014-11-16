/**
 * Copyright (C) 2014 Frank Steiler <frank@steilerdev.de>
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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public class Event
{
    @Id
    private String id;

    @NotNull
    private String name;
    private String description;
    private String location;

    @LastModifiedDate
    private Date lastChanged;

    @NotNull
    @Future
    @DateTimeFormat(iso= DateTimeFormat.ISO.DATE)
    private Date eventDate;

    @DBRef
    private List<Division> invitedDivision;

    public Event() {}

    public Event(String name, String description, String location, Date lastChanged, Date eventDate, List<Division> invitedDivision)
    {
        this.name = name;
        this.description = description;
        this.location = location;
        this.lastChanged = lastChanged;
        this.eventDate = eventDate;
        this.invitedDivision = invitedDivision;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public Date getLastChanged()
    {
        return lastChanged;
    }

    public void setLastChanged(Date lastChanged)
    {
        this.lastChanged = lastChanged;
    }

    public Date getEventDate()
    {
        return eventDate;
    }

    public void setEventDate(Date eventDate)
    {
        this.eventDate = eventDate;
    }

    public List<Division> getInvitedDivision()
    {
        return invitedDivision;
    }

    public void setInvitedDivision(List<Division> invitedDivision)
    {
        this.invitedDivision = invitedDivision;
    }
}
