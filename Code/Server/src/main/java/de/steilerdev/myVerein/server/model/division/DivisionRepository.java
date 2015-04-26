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
package de.steilerdev.myVerein.server.model.division;

import de.steilerdev.myVerein.server.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * This interface is used to query the database for specific division object. The repository is implemented during runtime by SpringData through the @Repository annotation.
 */
@Repository
public interface DivisionRepository extends MongoRepository<Division, String> {

    Division findByName(String name);
    Division findById(String id);
    List<Division> findByAdminUser(User adminUser);
    List<Division> findByParent(Division parent);
    List<Division> findByAncestors(Division ancestors);

    /**
     * Gathers all division names available.
     * @return All divisions, but only their name field is populated.
     */
    @Query(value="{}", fields="{ 'name' : 1}")
    List<Division> findAllNames();

    /**
     * Gathers all divisions, where the name contains the specified string.
     * @param contains The returned division needs to contain the specified String.
     * @return All divisions, containing the specified String, but only their name field is populated.
     */
    @Query(value="{'_id': { $regex: '.*?0.*', $options: 'i' }}", fields="{ 'name' : 1}")
    List<Division> findAllNamesContainingString(String contains);
}