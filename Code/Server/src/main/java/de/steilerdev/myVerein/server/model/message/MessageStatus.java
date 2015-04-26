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
package de.steilerdev.myVerein.server.model.message;

/**
 * This enum is representing the status of a message sent to a specific receiver.
 */
public enum MessageStatus {
    /**
     * This status is assigned to a message which is stored on the server but not delivered to the client yet
     */
    PENDING {
        @Override
        public String toString() {
            return "PENDING";
        }
    },
    /**
     * This status is assigned to a message which is delivered to the client
     */
    DELIVERED {
        @Override
        public String toString() {
            return "DELIVERED";
        }
    },
    /**
     * This status is assigned to a message which is read by the client
     */
    READ {
        @Override
        public String toString() {
            return "READ";
        }
    }
}
