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
import de.steilerdev.myVerein.server.controller.admin.DivisionManagementController;
import de.steilerdev.myVerein.server.security.PasswordEncoder;
import de.steilerdev.myVerein.server.security.UserAuthenticationService;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
        key = escapeCustomUserKey(key);
        if (customUserField == null)
        {
            customUserField = new HashMap<>();
            //Escaping a dot for the key is needed
            customUserField.put(key, value);
        }else if(overwrite || !customUserField.keySet().contains(key))
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
            customUserField.remove(escapeCustomUserKey(key));
        }
    }

    /**
     * This function renames a custom user field.
     * @param oldKey The unescaped old key.
     * @param newKey The unescaped new key.
     */
    public void renameCustomUserField(String oldKey, String newKey)
    {
        if(customUserField != null && customUserField.get(escapeCustomUserKey(oldKey)) != null)
        {
            logger.debug("Moving " + customUserField.get(escapeCustomUserKey(oldKey)) + " from " + oldKey + " to " + newKey);
            customUserField.put(escapeCustomUserKey(newKey), customUserField.get(escapeCustomUserKey(oldKey)));
            customUserField.remove(escapeCustomUserKey(oldKey));
        }
    }

    /**
     * This function returns the unescaped map of custom user fields
     * @return The unescaped custom user field map
     */
    public Map<String, String> getCustomUserField()
    {
        if(customUserField != null)
        {
            //Returning a map, containing unescaped characters
            return customUserField.keySet().parallelStream().collect(Collectors.toMap(User::unEscapeCustomUserKey, customUserField::get));
        } else
        {
            return null;
        }
    }

    public void unEscapeCustomUserField()
    {
        customUserField = getCustomUserField();
    }

    /**
     * This function sets the custom user fields and escapes the key field.
     * @param customUserField The new custom user field map.
     */
    public void setCustomUserField(Map<String, String> customUserField)
    {
        if(customUserField != null)
        {
            this.customUserField = customUserField.keySet().parallelStream().collect(Collectors.toMap(User::escapeCustomUserKey, customUserField::get));
        } else
        {
            this.customUserField = null;
        }
    }

    public List<Division> getDivisions()
    {
        return divisions;
    }

    public void setDivisions(List<Division> divisions)
    {
        this.divisions = divisions;
    }

    public void addDivision(Division division)
    {
        if(divisions == null)
        {
            divisions = new ArrayList<>();
            divisions.add(division);
        } else if(!divisions.contains(division))
        {
            divisions.add(division);
        }
    }

    public void removeDivision(Division division)
    {
        if(divisions != null && !divisions.isEmpty())
        {
            divisions.remove(division);
        }
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

    public void removeEverythingExceptId()
    {
        removeEverythingExceptEmailAndName();
        firstName = null;
        lastName = null;
        email = null;
    }

    /**
     * This function removes all unnecessary information besides the email and name of a user.
     */
    public void removeEverythingExceptEmailAndName()
    {
        customUserField = null;
        divisions = null;

        activeSince = null;
        passiveSince = null;
        resignationDate = null;
        birthday = null;

        gender  = null;

        zipCode = null;
        city = null;
        country = null;
        street = null;
        streetNumber = null;

        iban = null;
        bic = null;

        membershipStatus = null;
        administrationNotAllowedMessage = null;
    }

    /**
     * This function removes all private information (information only the administrator of the user is allowed to see)
     */
    public void removePrivateInformation()
    {
        customUserField = null;
        bic = null;
        iban = null;
        birthday = null;
        street = null;
        streetNumber = null;
        city = null;
        zipCode = null;
    }

    /**
     * This function checks if the current user is allowed to modify a specified event. The user is allowed to modify the event, if he is either the creator of the event or the superadmin.
     * @param event The selected event.
     * @return True if the user is allowed, false otherwise.
     */
    @JsonIgnore
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
                    DivisionManagementController.getOptimizedSetOfDivisions(divisionRepository.findByAdminUser(this)) //Getting all divisions administrated by the user, should not be empty, since the user is an admin
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
    public boolean isAdmin()
    {
        return authorities != null &&
               authorities.parallelStream()
                    .anyMatch(authority -> authority.getAuthority().equals(UserAuthenticationService.AuthorityRoles.ADMIN.toString()) ||
                                        authority.getAuthority().equals(UserAuthenticationService.AuthorityRoles.SUPERADMIN.toString()));
    }

    /**
     * This function escapes non allowed characters within the key of a custom user field.
     * @param source The key of a custom user field.
     * @return The escaped key of a custom user field.
     */
    public static String escapeCustomUserKey(String source)
    {
        if(source.contains("."))
        {
            source = source.replace(".", "[dot]");
        }
        return source;
    }

    /**
     * This function un-escapes non allowed characters within the escaped key of a custom user field.
     * @param source The escaped key of a custom user field.
     * @return The un-escaped key of a custom user field.
     */
    public static String unEscapeCustomUserKey(String source)
    {
        if(source.contains("[dot]"))
        {
            source = source.replace("[dot]", ".");
        }
        return source;
    }

    /*

        Overwritten Java Object functions

     */

    @Override
    public String toString()
    {
        return email;
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
