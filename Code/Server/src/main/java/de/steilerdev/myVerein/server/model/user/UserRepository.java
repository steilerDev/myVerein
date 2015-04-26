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
package de.steilerdev.myVerein.server.model.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * This repository is used to store and retrieve user from the MongoDB. The repository is implemented during runtime by SpringData.
 */
public interface UserRepository extends MongoRepository<User, String> {

    User findByEmail(String email);
    User findById(String id);

    /**
     * This function gathers all user objects, only containing their first name, last name and email.
     * @return
     */
    @Query(value="{}", fields="{ 'firstName' : 1, 'lastName' : 1, 'email' : 1 }")
    List<User> findOnlyEmailAndName();

    /**
     * Gathers all user whose firstName, lastName or email contains the String.
     * @param contains The returned user contains this string on one of the following fields: firstName, lastName, email.
     * @return All user, containing the specified String, but only their firstName, lastName and email field is populated.
     */
    @Query(value="{$or : [{'_id': { $regex: '.*?0.*', $options: 'i' }}, {'firstName': { $regex: '.*?0.*', $options: 'i' }}, {'lastName': { $regex: '.*?0.*', $options: 'i' }}]}", fields="{ 'firstName' : 1, 'lastName' : 1, 'email' : 1}")
    List<User> findOnlyEmailAndNameContainingString(String contains);
}
