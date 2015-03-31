/**
 * Copyright (C) ${YEAR} Frank Steiler <frank@steilerdev.de>
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
import com.fasterxml.jackson.databind.util.ArrayIterator;
import de.steilerdev.myVerein.server.controller.admin.DivisionManagementController;
import de.steilerdev.myVerein.server.security.PasswordEncoder;
import de.steilerdev.myVerein.server.security.UserAuthenticationService;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.time.LocalDate;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This object is representing an entity within the user's collection of the MongoDB and is used by Spring Security as UserDetails implementation. On top of that the class is providing several useful helper methods.
 */
public class User implements UserDetails
{
    @Transient
    @JsonIgnore
    private static Logger logger = LoggerFactory.getLogger(User.class);

    public enum Gender {
        MALE,
        FEMALE
    }

    public enum MembershipStatus {
        ACTIVE,
        PASSIVE,
        RESIGNED
    }

    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String firstName;
    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lastName;

    @Id
    private String id;

    @Indexed
    @NotBlank
    @Email
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;

    @JsonIgnore
    @NotBlank
    private String password;

    @JsonIgnore
    @NotBlank
    private String salt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String,String> customUserField;

    @DBRef
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Division> divisions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate activeSince;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate passiveSince;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate resignationDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate birthday;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Gender gender;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String zipCode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String city;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String country;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String street;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String streetNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String iban;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bic;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MembershipStatus membershipStatus;

    @Transient
    @JsonIgnore
    Collection<? extends GrantedAuthority> authorities;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String administrationNotAllowedMessage;

    /*

    Standard function (Constructor and getter/setter)

     */

    public User() {}

    public User(String firstName, String lastName, String email, String password)
    {
        this(firstName, lastName, email, password, null);
        updateMembershipStatus();
    }

    public User(String firstName, String lastName, String email, String password, HashMap<String,String> customUserField)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.customUserField = customUserField;
        setPassword(password);
        updateMembershipStatus();
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName != null && !firstName.isEmpty() ? firstName : null;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName != null && !lastName.isEmpty() ? lastName : null;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email != null && !email.isEmpty() ? email : null;
    }

    public String getPassword()
    {
        return password;
    }

    /**
     * Setting the password. The password is automatically hashed and the salt is randomly re-generated.
     * @param password The new plain text password.
     */
    public void setPassword(String password)
    {
        salt = KeyGenerators.string().generateKey();
        PasswordEncoder passwordEncoder = new PasswordEncoder();
        this.password = passwordEncoder.encodePassword(password, salt);
    }

    public Map<String, String> getCustomUserField()
    {
        return customUserField;
    }

    public void setCustomUserField(Map<String, String> customUserField)
    {
        this.customUserField = customUserField;
    }

    public List<Division> getDivisions()
    {
        return divisions;
    }

    /**
     * This function sets the division list, not respecting the inverse list in the divisions objects. This function should only be used to repopulate the bean.
     * Use replaceDivision instead.
     * @param divisions The new list of divisions.
     */
    public void setDivisions(List<Division> divisions)
    {
        this.divisions = divisions;
    }

    public String getSalt()
    {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
    }

    public String getAdministrationNotAllowedMessage()
    {
        return administrationNotAllowedMessage;
    }

    public void setAdministrationNotAllowedMessage(String administrationNotAllowedMessage)
    {
        this.administrationNotAllowedMessage = administrationNotAllowedMessage;
    }

    public LocalDate getActiveSince()
    {
        return activeSince;
    }

    public void setActiveSince(LocalDate activeSince)
    {
        this.activeSince = activeSince;
        updateMembershipStatus();
    }

    public LocalDate getBirthday()
    {
        return birthday;
    }

    public void setBirthday(LocalDate birthday)
    {
        this.birthday = birthday;
    }

    public LocalDate getPassiveSince()
    {
        return passiveSince;
    }

    public void setPassiveSince(LocalDate passiveSince)
    {
        this.passiveSince = passiveSince;
        updateMembershipStatus();
    }

    public LocalDate getResignationDate()
    {
        return resignationDate;
    }

    public void setResignationDate(LocalDate resignationDate)
    {
        this.resignationDate = resignationDate;
        updateMembershipStatus();
    }

    public Gender getGender()
    {
        return gender;
    }

    public void setGender(Gender gender)
    {
        this.gender = gender;
    }

    public String getZipCode()
    {
        return zipCode;
    }

    public void setZipCode(String zipCode)
    {
        this.zipCode = zipCode != null && !zipCode.isEmpty() ? zipCode : null;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city != null && !city.isEmpty() ? city : null;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country != null && !country.isEmpty() ? country : null;
    }

    public String getStreet()
    {
        return street;
    }

    public void setStreet(String street)
    {
        this.street = street != null && !street.isEmpty() ? street : null;
    }

    public String getStreetNumber()
    {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber)
    {
        this.streetNumber = streetNumber;
    }

    public String getIban()
    {
        return iban;
    }

    public void setIban(String iban)
    {
        this.iban = iban != null && !iban.isEmpty() ? iban : null;
    }

    public String getBic()
    {
        return bic;
    }

    public void setBic(String bic)
    {
        this.bic = bic != null && !bic.isEmpty() ? bic : null;
    }

    public MembershipStatus getMembershipStatus()
    {
        return membershipStatus;
    }

    public void setMembershipStatus(MembershipStatus membershipStatus)
    {
        this.membershipStatus = membershipStatus;
    }

    /*

        Other getter/setter functions

     */

    /**
     * This function adds a custom user field and overwrites the existing value.
     * @param key The unescaped key of the field.
     * @param value The value of the field.
     */
    public void addCustomUserField(String key, String value)
    {
        addCustomUserField(key, value, true);
    }

    /**
     * This function adds a custom user field to the user
     * @param key The unescaped key of the custom field
     * @param value The value of the custom field
     * @param overwrite The flag, indicating if an existing value should be overwritten (true) or not
     */
    public void addCustomUserField(String key, String value, boolean overwrite)
    {
        if (customUserField == null)
        {
            customUserField = new HashMap<>();
            customUserField.put(key, value);
        } else if(overwrite || !customUserField.keySet().contains(key))
        {
            //Escaping a dot for the key is needed
            customUserField.put(key, value);
        }
    }

    /**
     * This function removes a custom user field specified by the key.
     * @param key The unescaped key of the custom user field.
     */
    public void removeCustomUserField(String key)
    {
        if(customUserField != null)
        {
            customUserField.remove(key);
        }
    }

    /**
     * This function renames a custom user field.
     * @param oldKey The unescaped old key.
     * @param newKey The unescaped new key.
     */
    public void renameCustomUserField(String oldKey, String newKey)
    {
        if(customUserField != null && customUserField.get(oldKey) != null)
        {
            logger.debug("Moving " + customUserField.get(oldKey) + " from " + oldKey + " to " + newKey);
            customUserField.put(newKey, customUserField.get(oldKey));
            customUserField.remove(oldKey);
        }
    }

    /**
     * This function replaces the set of divisions by the stated divisions. The function guarantees that the inverse membership is handled correctly.
     * @param divisionRepository The division repository needed to save the altered divisions.
     * @param eventRepository The event repository needed to save the altered events.
     * @param divs The new list of divisions for the user.
     */
    public void replaceDivisions(DivisionRepository divisionRepository, EventRepository eventRepository, Division... divs)
    {
        replaceDivisions(divisionRepository, eventRepository, Arrays.asList(divs));
    }

    /**
     * This function replaces the set of divisions by the stated divisions. The function guarantees that the inverse membership is handled correctly.
     * @param divisionRepository The division repository needed to save the altered divisions.
     * @param eventRepository The event repository needed to save the altered events.
     * @param divs The new list of divisions for the user.
     */
    public void replaceDivisions(DivisionRepository divisionRepository, EventRepository eventRepository, List<Division> divs)
    {
        List<Division> finalDivisions = Division.getExpandedSetOfDivisions(divs, divisionRepository);
        List<Division> oldDivisions = divisions;

        if((finalDivisions == null || finalDivisions.isEmpty()) && (oldDivisions == null || oldDivisions.isEmpty()))
        {
            logger.debug("Division sets before and after are both empty");
            divisions = new ArrayList<>();
        } else if(finalDivisions == null || finalDivisions.isEmpty())
        {
            logger.debug("Division set after is empty, before is not. Removing membership subscription from old divisions.");
            oldDivisions.stream().forEach(div -> div.removeMember(this));
            divisionRepository.save(oldDivisions);

            //Updating events, affected by division change
            oldDivisions.parallelStream().forEach(div -> {
                List<Event> changedEvents = eventRepository.findAllByInvitedDivision(div);
                changedEvents.parallelStream().forEach(event -> event.updateInvitedUser(divisionRepository));
                eventRepository.save(changedEvents);
            });
            divisions = new ArrayList<>();
        } else if(oldDivisions == null || oldDivisions.isEmpty())
        {
            logger.debug("Division set before is empty, after is not. Adding membership subscription to new divisions.");
            finalDivisions.stream().forEach(div -> div.addMember(this));
            divisionRepository.save(finalDivisions);

            //Updating events, affected by division change
            finalDivisions.parallelStream().forEach(div -> {
                List<Event> changedEvents = eventRepository.findAllByInvitedDivision(div);
                changedEvents.parallelStream().forEach(event -> event.updateInvitedUser(divisionRepository));
                eventRepository.save(changedEvents);
            });
            divisions = finalDivisions;
        } else
        {
            logger.debug("Division set after and before are not empty. Applying changed membership subscriptions.");
            List<Division> intersect = finalDivisions.stream().filter(oldDivisions::contains).collect(Collectors.toList()); //These items are already in the list, and do not need to be modified

            //Collecting changed division for batch save
            List<Division> changedDivisions = Collections.synchronizedList(new ArrayList<>());

            //Removing membership from removed divisions
            oldDivisions.parallelStream()
                    .filter(div -> !intersect.contains(div))
                    .forEach(div -> {
                        div.removeMember(this);
                        changedDivisions.add(div);
                    });

            //Adding membership to added divisions
            finalDivisions.parallelStream()
                    .filter(div -> !intersect.contains(div))
                    .forEach(div -> {
                        div.addMember(this);
                        changedDivisions.add(div);
                    });

            divisionRepository.save(changedDivisions);

            //Updating events, affected by division change
            changedDivisions.parallelStream().forEach(div -> {
                List<Event> changedEvents = eventRepository.findAllByInvitedDivision(div);
                changedEvents.parallelStream().forEach(event -> event.updateInvitedUser(divisionRepository));
                eventRepository.save(changedEvents);
            });
            divisions = finalDivisions;
        }
    }

    /*

        Spring security functions

     */

    /**
     * Function needed by Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails}.
     * @return The list of GrantedAuthorities of the user.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return authorities;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities)
    {
        this.authorities = authorities;
    }

    /**
     * Function needed by Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails}.
     * @return The email of the user.
     */
    @JsonIgnore
    @Override
    public String getUsername()
    {
        return email;
    }

    /**
     * Function needed by Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails}.
     * @return Always true at the moment.
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    /**
     * Function needed by Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails}.
     * @return Always true at the moment.
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    /**
     * Function needed by Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails}.
     * @return Always true at the moment.
     */
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    /**
     * Function needed by Spring Security's {@link org.springframework.security.core.userdetails.UserDetails UserDetails}.
     * @return Always true at the moment.
     */
    @JsonIgnore
    @Override
    public boolean isEnabled()
    {
        return true;
    }

    /*

        Helper functions needed by the application

     */

    /**
     * This function checks the stated dates of a user and sets the current membership status accordingly. This function is invoked, as soon as one of the dates is changed.
     */
    public void updateMembershipStatus()
    {
        if(activeSince != null)
        {   //Is it there or irrelevant
            if(passiveSince == null || passiveSince.isBefore(activeSince))
            {
                if(resignationDate == null || resignationDate.isBefore(activeSince))
                {
                    membershipStatus = MembershipStatus.ACTIVE;
                } else
                {
                    membershipStatus = MembershipStatus.RESIGNED;
                }
            } else
            {
                if(resignationDate == null || resignationDate.isBefore(passiveSince))
                {
                    membershipStatus = MembershipStatus.PASSIVE;
                } else
                {
                    membershipStatus = MembershipStatus.RESIGNED;
                }
            }
        } else
        {
            if(passiveSince != null)
            {
                if(resignationDate == null || resignationDate.isBefore(passiveSince))
                {
                    membershipStatus = MembershipStatus.PASSIVE;
                } else
                {
                    membershipStatus = MembershipStatus.RESIGNED;
                }
            } else if(resignationDate != null)
            {
                membershipStatus = MembershipStatus.RESIGNED;
            } else
            {
                membershipStatus = null;
            }
        }
    }

    /**
     * This function creates a new user object and copies only the id of the current user.
     * @return A new user object only containing the id.
     */
    @JsonIgnore
    @Transient
    public User getSendingObjectOnlyId()
    {
        User sendingObject = new User();
        sendingObject.setId(id);
        return sendingObject;
    }

    /**
     * This function creates a new user object and copies only the email, name and id of the current user.
     * @return A new user object only containing email, name and id.
     */
    @JsonIgnore
    @Transient
    public User getSendingObjectOnlyEmailNameId()
    {
        User sendingObject = new User();
        sendingObject.setEmail(email);
        sendingObject.setFirstName(firstName);
        sendingObject.setLastName(lastName);
        sendingObject.setId(id);
        return sendingObject;
    }

    /**
     * This function creates a new user object, by copying the properties of the current object, ignoring the private information.
     * @return A copied user object, without the private information.
     */
    @JsonIgnore
    @Transient
    public User getSendingObjectNoPrivateInformation()
    {
        String[] ignoredProperties = {
                "customUserField",
                "bic",
                "iban",
                "birthday",
                "street",
                "streetNumber",
                "city",
                "zipCode"
        };
        User sendingObject = getSendingObject(ignoredProperties);
        if(sendingObject.getDivisions() != null)
        {
            sendingObject.getDivisions().replaceAll(Division::getSendingObjectInternalSync);
        }

        return sendingObject;
    }

    /**
     * This function removes all fields that the other users of the app are not allowed to see.
     * @return A copied user object, without the fields, other users are not allowed to see.
     */
    @JsonIgnore
    @Transient
    public User getSendingObjectInternalSync()
    {
        String[] ignoriedProperties = {
                "customUserFields",
                "activeSince",
                "passiveSince",
                "resignationDate",
                "iban",
                "bic",
                "administrationNotAllowed"
        };

        User sendingObject = getSendingObject(ignoriedProperties);
        if(sendingObject.getDivisions() != null)
        {
            sendingObject.getDivisions().replaceAll(Division::getSendingObjectOnlyId);
        }

        return sendingObject;
    }

    /**
     * This function creates a sending save object (ensuring there is no infinite loop caused by the admin user references)
     * @return A sending save instance of the object
     */
    @JsonIgnore
    @Transient
    public User getSendingObject()
    {
        User sendingObject = this.getSendingObject(new String[0]);
        if(sendingObject.getDivisions() != null)
        {
            sendingObject.getDivisions().replaceAll(Division::getSendingObject);
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
    private User getSendingObject(String... ignoredProperties)
    {
        User sendingObject = new User();
        BeanUtils.copyProperties(this, sendingObject, ignoredProperties);
        return sendingObject;
    }

    /**
     * This function checks if the current user is allowed to modify a specified event. The user is allowed to modify the event, if he is either the creator of the event or the superadmin.
     * @param event The selected event.
     * @return True if the user is allowed, false otherwise.
     */
    @JsonIgnore
    @Transient
    public boolean isAllowedToAdministrate(Event event)
    {
        return this.isAdmin() &&
                (
                        this.equals(event.getEventAdmin()) ||
                        this.isSuperAdmin() ||
                        event.getEventAdmin() == null //If there is no admin for the event it is okay to manipulate the event (Although this should not happen)
                );
    }

    /**
     * This function checks if the user is allowed to administrate (view private information and change user details) the selected user. The user is allowed to administrate the user, if he is the super admin, or if he is administrating a division, the user is part of.
     * @param selectedUser The selected user.
     * @param divisionRepository The division repository used to retrieve all divisions information, due to a problem of injecting the resource within the user object.
     * @return True if the user is allowed, false otherwise.
     */
    @JsonIgnore
    @Transient
    public boolean isAllowedToAdministrate(User selectedUser, DivisionRepository divisionRepository)
    {
        //Getting the list of administrated divisions
        List<Division> administratedDivisions = divisionRepository.findByAdminUser(this);

        return this.isAdmin() && //First of all the user needs to be an administrator
                selectedUser != null && //The user needs to be present
                (
                    this.isSuperAdmin() || //If the user is the super admin he can do whatever he wants
                    selectedUser.getDivisions() == null ||  //If there is no divisions or
                    selectedUser.getDivisions().isEmpty() ||  //the list of divisions is empty, the user is allowed to administrate the user
                    selectedUser.equals(this) || //If the user is the same he is allowed
                    selectedUser.getDivisions().parallelStream() //Streaming all divisions the user is part of
                            .anyMatch(div -> //If there is any match the admin is allowed to view the user
                                    div.getAncestors().parallelStream() //Streaming all ancestors of the user's divisions
                                            .anyMatch(anc -> administratedDivisions.contains(anc))) //If there is any match between administrated divisions and ancestors of one of the users divisions
                );
    }

    /**
     * This function checks if the user is allowed to administrate a selected division. The user is allowed to administrate the division, if he is the super admin, or administrating the division or a parent division.
     * @param division The selected division.
     * @param divisionRepository The division repository used to retrieve all divisions information, due to a problem of injecting the resource within the user object.
     * @return True if the user is allowed, false otherwise.
     */
    @JsonIgnore
    @Transient
    public boolean isAllowedToAdministrate(Division division, DivisionRepository divisionRepository)
    {
        return this.isAdmin() && //The user needs to be an administrator
                division != null && //The division needs to be present
                (
                    this.isSuperAdmin() || //If the user is a super admin he can do whatever he wants
                    Division.getOptimizedSetOfDivisions(divisionRepository.findByAdminUser(this)) //Getting all divisions administrated by the user, should not be empty, since the user is an admin
                        .parallelStream().anyMatch(div -> div.equals(division) ||  //If the selected division is one of the administrated ones
                                                          division.getAncestors().contains(div)) //If the selected division is an ancestor of the administrated ones
                );
    }

    /**
     * This function checks if the current user has the role superadmin.
     * @return True if the user is the super admin, false otherwise.
     */
    @JsonIgnore
    @Transient
    public boolean isSuperAdmin()
    {
        return authorities != null &&
               authorities.parallelStream()
                    .anyMatch(authority -> authority.getAuthority().equals(UserAuthenticationService.AuthorityRoles.SUPERADMIN.toString()));
    }

    /**
     * This function checks if the user is an administrator. The user is an administrator, if he is administrating at least one division.
     * @return Tue if the user is an administrator, false otherwise.
     */
    @JsonIgnore
    @Transient
    public boolean isAdmin()
    {
        return authorities != null &&
               authorities.parallelStream()
                    .anyMatch(authority -> authority.getAuthority().equals(UserAuthenticationService.AuthorityRoles.ADMIN.toString()) ||
                                        authority.getAuthority().equals(UserAuthenticationService.AuthorityRoles.SUPERADMIN.toString()));
    }

    /*

        Overwritten Java Object functions

     */

    @Override
    public String toString()
    {
        return (email != null && !email.isEmpty())
                ? email
                : (id != null && !id.isEmpty())
                    ? id
                    : "N/A";
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && obj instanceof User && this.id != null && this.id.equals(((User) obj).getId());
    }

    @Override
    public int hashCode()
    {
        return id == null? 0: id.hashCode();
    }
}
