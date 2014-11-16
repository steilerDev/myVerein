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

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public class Picture
{
    @Id
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private URI url;

    private String description;
    private List<String> tags;

    @DBRef
    @NotNull
    private User uploader;

    @DBRef
    private Division division;

    public Picture() {}

    public Picture(String name, URI url, String description, List<String> tags, User uploader, Division division)
    {
        this.name = name;
        this.url = url;
        this.description = description;
        this.tags = tags;
        this.uploader = uploader;
        this.division = division;
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

    public URI getUrl()
    {
        return url;
    }

    public void setUrl(URI url)
    {
        this.url = url;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public User getUploader()
    {
        return uploader;
    }

    public void setUploader(User uploader)
    {
        this.uploader = uploader;
    }

    public Division getDivision()
    {
        return division;
    }

    public void setDivision(Division division)
    {
        this.division = division;
    }
}
