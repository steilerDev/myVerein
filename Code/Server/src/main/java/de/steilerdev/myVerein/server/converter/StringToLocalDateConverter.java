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

import java.time.LocalDate;

/**
 * This class is a converter used by SpringData, to convert a String to a Java 8 LocalDate object.
 */
@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate>
{
    /**
     * This function uses the {@link java.time.LocalDate#parse parse} method of LocalDate, to convert the source String to a LocalDate object.
     * @param source The source String.
     * @return The object representation of the String.
     */
    @Override
    public LocalDate convert(String source) {
        return source == null ? null : LocalDate.parse(source);
    }
}