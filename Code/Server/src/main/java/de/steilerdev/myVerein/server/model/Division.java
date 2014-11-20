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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Division
{
    @Id
    @Indexed
    @NotBlank
    private String name;

    private String desc;

    @DBRef
    private User adminUser;

    @DBRef
    private Division parent;

    @DBRef
    private List<Division> ancestors;

    @Transient
    private static Logger logger = LoggerFactory.getLogger(Division.class);

    public Division(){}

    public Division(String name, String desc, User adminUser, Division parent, List<Division> ancestors)
    {
        this.name = name;
        this.desc = desc;
        this.adminUser = adminUser;
        this.parent = parent;
        this.ancestors = ancestors;
    }

    public Division(String name, String desc, User adminUser, Division parent)
    {
        this.name = name;
        this.desc = desc;
        this.adminUser = adminUser;
        this.parent = parent;

        if(parent != null)
        {
            List<Division> ancestor;
            if (parent.getAncestors() == null)
            {
                ancestor = new ArrayList<>();
            } else
            {
                //Need to create a new ArrayList, assigning would lead to fill and use BOTH lists
                ancestor = new ArrayList<>(parent.getAncestors());
            }
            ancestor.add(parent);
            logger.debug("Ancestors " + ancestor.stream().map(div -> div.getName()).collect(Collectors.joining(", ")) + " for division " + this.name);
            this.ancestors = ancestor;
        }
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

    public void addAncestor(Division ancestor)
    {
        logger.debug("Adding ancestor " + ancestor.getName() + " to " + this.name);
        if(ancestors == null)
        {
            ancestors = new ArrayList<Division>();
        }
        ancestors.add(ancestor);
    }

    public void setAncestors(List<Division> ancestors)
    {
        this.ancestors = ancestors;
    }

    /**
     * Comparing two objects of the division class according to their name. Overwritten to be able to use the contains() method of java.util.List
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof Division)
        {
            sameSame = ((Division) object).name.equals(this.name);
        }

        return sameSame;
    }
}
