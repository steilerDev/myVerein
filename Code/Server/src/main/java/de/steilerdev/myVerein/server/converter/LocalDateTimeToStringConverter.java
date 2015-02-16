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
package de.steilerdev.myVerein.server.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * This class is a converter used by SpringData, to convert a Java 8 LocalDateTime to a String.
 */
@Component
public class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String>
{
    /**
     * This function uses the {@link java.time.LocalDateTime#toString toString} method of LocalDateTime, to convert the source object to a String.
     * @param source The source LocalDateTime object.
     * @return The String representation of the source object.
     */
    @Override
    public String convert(LocalDateTime source) {
        return source == null ? null : source.toString();
    }
}