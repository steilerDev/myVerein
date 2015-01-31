/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
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
package de.steilerdev.myVerein.server.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsController
{
    public void settings()
    {
        try
        {
            Resource resource = new ClassPathResource("mongo.properties");
            Properties props = PropertiesLoaderUtils.loadProperties(resource);
            System.err.println(props.toString());
            props.setProperty("dbPort", "27018");
            props.store(new FileOutputStream(resource.getFile()), "comment");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
