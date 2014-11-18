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

import de.steilerdev.myVerein.server.security.PasswordEncoder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.keygen.KeyGenerators;

import java.util.*;

public class User implements UserDetails
{
    @Id
    private String id;

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
    @NotBlank
    private String salt;

    private HashMap<String,String> privateInformation;
    private HashMap<String,String> publicInformation;

    @DBRef
    private List<Division> divisions;

    @Transient
    Collection<? extends GrantedAuthority> authorities;

    public User() {}

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
        return String.format("User[id=%s, firstName=%s, lastName=%s", id, firstName, lastName);
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
    @Override
    public String getUsername()
    {
        return email;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
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

    public HashMap<String, String> getPrivateInformation()
    {
        return privateInformation;
    }

    public void setPrivateInformation(HashMap<String, String> privateInformation)
    {
        this.privateInformation = privateInformation;
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
        }
        divisions.add(division);
    }

    public String getSalt()
    {
        return salt;
    }

    public void setSalt(String salt)
    {
        this.salt = salt;
    }
}
