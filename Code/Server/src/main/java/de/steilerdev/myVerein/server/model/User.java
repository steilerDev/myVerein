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

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.List;

public class User
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

    private HashMap<String,String> privateInformation;
    private HashMap<String,String> publicInformation;

    @NotEmpty
    private List<HashMap<String,Object>> divisions;

    public User() {}

    public User(String firstName, String lastName, String email, String password, HashMap<String,String> privateInformation, HashMap<String,String> publicInformation)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.privateInformation = privateInformation;
        this.publicInformation = publicInformation;
    }

    @Override
    public String toString()
    {
        return String.format("User[id=%s, firstName=%s, lastName=%s", id, firstName, lastName);
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

    public void setPassword(String password)
    {
        this.password = password;
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

    public List<HashMap<String, Object>> getDivisions()
    {
        return divisions;
    }

    public void setDivisions(List<HashMap<String, Object>> divisions)
    {
        this.divisions = divisions;
    }
}
