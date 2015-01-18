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
import de.steilerdev.myVerein.server.security.PasswordEncoder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class User implements UserDetails
{
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    @Id
    @Indexed
    @NotBlank
    @Email
    private String email;

    @JsonIgnore
    @NotBlank
    private String password;

    @JsonIgnore
    @NotBlank
    private String salt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<String,String> privateInformation;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<String,String> publicInformation;

    @DBRef
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Division> divisions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate memberSince;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate passiveSince;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate birthday;

    @Transient
    @JsonIgnore
    Collection<? extends GrantedAuthority> authorities;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    boolean administrationAllowed;

    public User() {}

    public User(String firstName, String lastName, String email, String password)
    {
        this(firstName, lastName, email, password, null, null);
    }

    public User(String firstName, String lastName, String email, String password, HashMap<String,String> privateInformation, HashMap<String,String> publicInformation)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.privateInformation = privateInformation;
        this.publicInformation = publicInformation;
        setPassword(password);
    }

    @Override
    public String toString()
    {
        return String.format("User[Email=%s, firstName=%s, lastName=%s]", email, firstName, lastName);
    }

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
     * Returns the username (Email address) of the selected user.
     * @return The username (Email address) of the user.
     */
    @JsonIgnore
    @Override
    public String getUsername()
    {
        return email;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled()
    {
        return true;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    /**
     * Setting the password. The password is automatically hashed and the salt is randomly generated.
     * @param password The new plain text password.
     */
    public void setPassword(String password)
    {
        salt = KeyGenerators.string().generateKey();
        PasswordEncoder passwordEncoder = new PasswordEncoder();
        this.password = passwordEncoder.encodePassword(password, salt);
    }

    public void addPrivateInformation(String key, String value)
    {
        if (privateInformation == null)
        {
            privateInformation = new HashMap<>();
        }
        privateInformation.put(key, value);
    }

    public HashMap<String, String> getPrivateInformation()
    {
        return privateInformation;
    }

    public void setPrivateInformation(HashMap<String, String> privateInformation)
    {
        this.privateInformation = privateInformation;
    }

    public void addPublicInformation(String key, String value)
    {
        if (publicInformation == null)
        {
            publicInformation = new HashMap<>();
        }
        publicInformation.put(key, value);
    }

    public HashMap<String, String> getPublicInformation()
    {
        return publicInformation;
    }

    public void setPublicInformation(HashMap<String, String> publicInformation)
    {
        this.publicInformation = publicInformation;
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

    public String getSalt()
    {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
    }

    public boolean isAdministrationAllowed()
    {
        return administrationAllowed;
    }

    public void setAdministrationAllowed(boolean administrationAllowed)
    {
        this.administrationAllowed = administrationAllowed;
    }

    public LocalDate getMemberSince()
    {
        return memberSince;
    }

    public void setMemberSince(LocalDate memberSince)
    {
        this.memberSince = memberSince;
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
    }
}
