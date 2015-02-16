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
 * This class is a converter used by SpringData, to convert a Java 8 LocalDate to a String.
 */
@Component
public class LocalDateToStringConverter implements Converter<LocalDate, String>
{
    /**
     * This function uses the {@link java.time.LocalDate#toString toString} method of LocalDate, to convert the source object to a String.
     * @param source The source LocalDate object.
     * @return The String representation of the source object.
     */
    @Override
    public String convert(LocalDate source) {
        return source == null ? null : source.toString();
    }
}