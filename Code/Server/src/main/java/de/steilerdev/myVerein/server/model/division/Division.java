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
package de.steilerdev.myVerein.server.model.division;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.steilerdev.myVerein.server.model.BaseEntity;
import de.steilerdev.myVerein.server.model.User;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.LazyLoadingException;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.*;

/**
 * This object is representing an entity within the division's collection of the MongoDB. On top of that the class is providing several useful helper methods.
 */
public class Division extends BaseEntity implements Comparable<Division>
{
    @JsonIgnore
    @Transient
    private final Logger logger = LoggerFactory.getLogger(Division.class);

    @Indexed
    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String desc;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @DBRef(lazy = true)
    private User adminUser;

    @JsonIgnore
    @DBRef(lazy = true)
    private Division parent;

    @JsonIgnore
    @DBRef
    private List<Division> ancestors;

    @JsonIgnore
    private List<String> memberList;

    /*
        Constructors (Empty one to meet bean definition and convenience ones)
     */

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
        this.replaceParent(parent);
    }

    /*
        Mandatory getter and setter
     */

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

    public List<String> getMemberList()
    {
        return memberList;
    }

    public void setMemberList(List<String> memberList)
    {
        this.memberList = memberList;
    }

    public List<Division> getAncestors()
    {
        if(ancestors == null)
        {
            return new ArrayList<>();
        }
        return ancestors;
    }

    public void setAncestors(List<Division> ancestors)
    {
        this.ancestors = ancestors;
    }

    /*
        Convenience getter and setter
     */

    /**
     * This function adds a member to the member list, if he is not already in the list.
     * @param user The new user.
     */
    public void addMember(User user)
    {
        if(memberList == null)
        {
            memberList = new ArrayList<>();
            memberList.add(user.getId());
        } else if (!memberList.contains(user.getId()))
        {
            memberList.add(user.getId());
        }
    }

    /**
     * This function removes a member from the member list, if he is in the list.
     * @param user The user that should be deleted.
     */
    public void removeMember(User user)
    {
        if(memberList != null && !memberList.isEmpty())
        {
            memberList.remove(user.getId());
        }
    }

    /**
     * This function updates the parent and the ancestors of this division.
     * @param parent The new parent.
     */
    public void replaceParent(Division parent)
    {
        logger.trace("Changing parent for {}", this);
        this.parent = parent;
        if(parent != null)
        {
            logger.debug("Updating ancestors for {}", this);
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
            logger.debug("Ancestors {} for division {}", ancestors, this);
            this.ancestors = ancestor;
        } else
        {
            this.ancestors = null;
        }
        logger.info("Successfully updated parent and ancestors of " + this.name);
    }

    /*
        Sending object functions
     */

    /**
     * This function creates a new division object and copies only the id of the current division.
     * @return A new division object only containing the id.
     */
    @JsonIgnore
    @Transient
    public Division getSendingObjectOnlyId()
    {
        Division sendingObject = new Division();
        sendingObject.setId(id);
        return sendingObject;
    }

    /**
     * This function creates a new division object and copies only the id and name of the current division.
     * @return A new division object only containing the id and name.
     */
    @JsonIgnore
    @Transient
    public Division getSendingObjectOnlyIdAndName()
    {
        Division sendingObject = new Division();
        sendingObject.setId(id);
        sendingObject.setName(name);
        return sendingObject;
    }

    /**
     * This function removes all fields that the other users of the app are not allowed to see.
     * @return A copied division object, without the fields, other users are not allowed to see.
     */
    @JsonIgnore
    @Transient
    public Division getSendingObjectInternalSync()
    {
        return getSendingObject();
    }

    /**
     * This function creates a sending-save object (ensuring there is no infinite loop caused by references)
     * @return A sending-save instance of the object.
     */
    @JsonIgnore
    @Transient
    public Division getSendingObject()
    {
        Division sendingObject = getSendingObject(new String[0]);
        if(sendingObject.getAdminUser() != null)
        {
            sendingObject.setAdminUser(sendingObject.getAdminUser().getSendingObjectOnlyEmailNameId());
        }
        return sendingObject;
    }

    /**
     * This function copies the current object, ignoring the member fields specified by the ignored properties vararg.
     * @param ignoredProperties The member fields ignored during the copying.
     * @return A copy of the current object, not containing information about the ignored properties.
     */
    @JsonIgnore
    @Transient
    private Division getSendingObject(String... ignoredProperties)
    {
        Division sendingObject = new Division();
        BeanUtils.copyProperties(this, sendingObject, ignoredProperties);
        return sendingObject;
    }

    /*
        Required java object functions
     */

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

    @Override
    public String toString()
    {
        return name != null && !name.isEmpty()? name: id;
    }

    /**
     * This function is comparable to other divisions according to their distance to the root node.
     * @param o The division which is compared to the current division.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object. If o is null, 0 is returned.
     */
    @Override
    public int compareTo(Division o)
    {
        if(o != null)
        {
            try
            {
                List<Division> thisAncestors = this.getAncestors(),
                        otherAncestors = o.getAncestors();

                if (thisAncestors == null || thisAncestors.isEmpty())
                {
                    return 1;
                } else if (otherAncestors == null || otherAncestors.isEmpty())
                {
                    return -1;
                } else
                {
                    return Integer.compare(thisAncestors.size(), otherAncestors.size());
                }
            } catch (LazyLoadingException e)
            {
                logger.error("Unable to compare divisions {} {}: {}", o, this, e.getMessage());
                return 0;
            }
        } else
        {
            logger.error("Comparing {} with null division", this);
            return 0;
        }
    }
}
