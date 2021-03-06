/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package de.steilerdev.myVerein.server.model.user;

/**
 * This enum is representing the gender of a user.
 */
public enum Gender {
    /**
     * This gender is assigned to all male member.
     */
    MALE {
        @Override
        public String toString() {
            return "MALE";
        }
    },
    /**
     * This gender is assigned to all female member.
     */
    FEMALE {
        @Override
        public String toString() {
            return "FEMALE";
        }
    }
}
