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
package de.steilerdev.myVerein.server.model.message;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This interface is used to query the database for specific message object. The repository is implemented during runtime by SpringData.
 */
public interface MessageRepository extends MongoRepository<Message, String> {

    @Query(value = "{?0 : ?1 }")
    List<Message> findByPrefixedReceiverIDAndMessageStatus(String receiverID, MessageStatus messageStatus);

    @Query(value = "{$and: [{?0 : { $exists: true }}, {'timestamp': {$gt: ?1}}] }")
    List<Message> findByPrefixedReceiverIDAndTimestampAfter(String receiverID, LocalDateTime timestamp); //Maybe someday... (Using this method you could enable multi device)
}
