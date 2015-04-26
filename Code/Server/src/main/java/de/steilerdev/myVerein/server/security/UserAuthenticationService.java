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
package de.steilerdev.myVerein.server.security;

import de.steilerdev.myVerein.server.model.division.Division;
import de.steilerdev.myVerein.server.model.division.DivisionRepository;
import de.steilerdev.myVerein.server.model.user.User;
import de.steilerdev.myVerein.server.model.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is implementing Spring's UserDetailsService to support the {@link User User} object as authentication principal.
 */
public class UserAuthenticationService implements UserDetailsService
{
    public enum AuthorityRoles {
        /**
         * This enum is representing a regular user, who is not administrating any divisions.
         */
        USER {
            @Override
            public String toString()
            {
                return "ROLE_USER";
            }
        },
        /**
         * This enum is representing an user, who is at least administrating one division.
         */
        ADMIN {
            @Override
            public String toString()
            {
                return "ROLE_ADMIN";
            }
        },
        /**
         * This enum is representing a user who is administrating the complete system.
         */
        SUPERADMIN {
            @Override
            public String toString()
            {
                return "ROLE_SUPERADMIN";
            }
        },
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    DivisionRepository divisionRepository;

    private final Logger logger = LoggerFactory.getLogger(UserAuthenticationService.class);

    /**
     * This function loads a user identified by his username (email address).
     * @param username The username of the user.
     * @return The specified user.
     * @throws UsernameNotFoundException If a user with the specified username could not be retrieved.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        logger.debug("[{}] Loading user", username);
        User user = userRepository.findByEmail(username);
        if(user == null)
        {
            logger.warn("Unable to find user with username " + username);
            throw new UsernameNotFoundException("Could not find user " + username);
        } else
        {
            user.setAuthorities(getUserAuthorities(user));
            return user;
        }
    }

    /**
     * Checks and returns the user authorities. The authorities are assigned according to the {@link de.steilerdev.myVerein.server.security.UserAuthenticationService.AuthorityRoles AuthorityRoles} enum.
     * @param user The user, whose authorities need to be checked.
     * @return A list of the roles of the user.
     */
    private ArrayList<GrantedAuthority> getUserAuthorities(User user)
    {
        logger.trace("[{}] Checking user's granted authorities", user);
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthorityRoles.USER.toString()));
        List<Division> administratedDiv = divisionRepository.findByAdminUser(user);
        if(!administratedDiv.isEmpty())
        {
            authorities.add(new SimpleGrantedAuthority(AuthorityRoles.ADMIN.toString()));
            if(administratedDiv.stream().anyMatch(div -> div.getParent() == null))
            {
                authorities.add(new SimpleGrantedAuthority(AuthorityRoles.SUPERADMIN.toString()));
            }
        }
        logger.info("[{}] Retrieved user authorities: {}", user, authorities);
        return authorities;
    }
}
