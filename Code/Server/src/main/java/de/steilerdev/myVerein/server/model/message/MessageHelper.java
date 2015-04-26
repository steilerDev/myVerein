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

import de.steilerdev.myVerein.server.model.user.User;

/**
 * This class contains static helper functions needed while handling messages.
 */
public class MessageHelper
{
    /**
     * {@link MessageRepository#findByPrefixedReceiverIDAndMessageStatus} needs a receiver id, prefixed with "receiver.", because a custom query with a fixed prefix is not working. This function creates this prefixed receiver id.
     * @param user The user, which needs to be prefixed.
     * @return The prefixed user ID.
     */
    public static String receiverIDForUser(User user) {
        return user == null? null: "receiver." + user.getId();
    }
}
