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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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

/**
 * This object is representing an entity within the division's collection of the MongoDB. On top of that the class is providing several useful helper methods.
 */
public class Division
{
    @Id
    private String id;

    @Indexed
    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String desc;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @DBRef
    private User adminUser;

    @JsonIgnore
    @DBRef
    private Division parent;

    @JsonIgnore
    @DBRef
    private List<Division> ancestors;

    @JsonIgnore
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
        this.setParent(parent);
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

    /**
     * This function updates the parent and the ancestors.
     * @param parent The new parent.
     */
    public void setParent(Division parent)
    {
        logger.trace("Changing parent for " + this.name);
        if(parent != null)
        {
            logger.debug("Updating ancestors for " + this.name);
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
        this.parent = parent;
        logger.info("Successfully updated parent and ancestors of " + this.name);
    }

    /**
     * @return The ancestor of this object. The function never returns null, but an empty list, if there are no ancestors defined.
     */
    public List<Division> getAncestors()
    {
        if(ancestors == null)
        {
            return new ArrayList<>();
        }
        return ancestors;
    }

    public void addAncestor(Division ancestor)
    {
        logger.debug("Adding ancestor " + ancestor.getName() + " to " + this.name);
        this.getAncestors().add(ancestor);
    }

    public void setAncestors(List<Division> ancestors)
    {
        this.ancestors = ancestors;
    }

    public void removeEverythingExceptId()
    {
        adminUser = null;
        desc = null;
        name = null;
    }

    /**
     * Comparing two objects of the division class according to their name. Overwritten to be able to use the contains() method of java.util.List.
     * @param obj The object compared to the current object.
     * @return True if the two objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof Division && this.id != null && this.id.equals(((Division) obj).getId());
    }

    @Override
    public int hashCode()
    {
        return id == null? 0: id.hashCode();
    }
}
