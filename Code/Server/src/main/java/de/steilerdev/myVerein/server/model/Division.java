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

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

public class Division
{
    @Id
    private String id;

    @NotBlank
    private String name;
    private String desc;

    @DBRef
    private User adminUser;

    @DBRef
    private Division parent;

    @DBRef
    private List<Division> ancestors;

    public Division(){}

    public Division(String name, String desc, User adminUser, Division parent, List<Division> ancestors)
    {
        this.name = name;
        this.desc = desc;
        this.adminUser = adminUser;
        this.parent = parent;
        this.ancestors = ancestors;
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

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public User getAdminUser()
    {
        return adminUser;
    }

    public void setAdminUser(User adminUser)
    {
        this.adminUser = adminUser;
    }

    public Division getParent()
    {
        return parent;
    }

    public void setParent(Division parent)
    {
        this.parent = parent;
    }

    public List<Division> getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(List<Division> ancestors)
    {
        this.ancestors = ancestors;
    }
}
