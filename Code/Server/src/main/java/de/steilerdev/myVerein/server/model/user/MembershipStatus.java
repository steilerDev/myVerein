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
 * This enum is representing the current membership status of a user.
 */
public enum MembershipStatus {
    /**
     * This value is assigned to all active members.
     */
    ACTIVE {
        @Override
        public String toString() {
            return "ACTIVE";
        }
    },
    /**
     * This value is assigned to all passive members.
     */
    PASSIVE {
        @Override
        public String toString() {
            return "PASSIVE";
        }
    },
    /**
     * This value is assigned to all resigned members.
     */
    RESIGNED {
        @Override
        public String toString() {
            return "RESIGNED";
        }
    }
}
