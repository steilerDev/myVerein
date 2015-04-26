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
package de.steilerdev.myVerein.server.model.event;

/**
 * This enum is representing the status of a message sent to a specific receiver.
 */
public enum EventStatus {
    /**
     * This status is assigned to an event which has not received any answer from a particular user
     */
    PENDING {
        @Override
        public String toString() {
            return "PENDING";
        }
    },
    /**
     * This status is assigned to an event where the user stated he would participate
     */
    GOING {
        @Override
        public String toString() {
            return "GOING";
        }
    },
    /**
     * This status is assigned to an event where the user stated he might participate
     */
    MAYBE {
        @Override
        public String toString() {
            return "MAYBE";
        }
    },
    /**
     * This status is assigned to an event where the user stated he is not participating
     */
    DECLINE {
        @Override
        public String toString() {
            return "DECLINE";
        }
    },
    /**
     * This status is assigned to an event where the user was previously invited, but left the division
     */
    REMOVED {
        @Override
        public String toString() {
            return "REMOVED";
        }
    }
}