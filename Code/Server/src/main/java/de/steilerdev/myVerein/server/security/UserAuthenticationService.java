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

import de.steilerdev.myVerein.server.model.Division;
import de.steilerdev.myVerein.server.model.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.model.UserRepository;
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

public class UserAuthenticationService implements UserDetailsService
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    DivisionRepository divisionRepository;

    private static Logger logger = LoggerFactory.getLogger(UserAuthenticationService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
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
     * Check the user authorities. If the user administrates a division he gets the user role "ROLE_ADMIN",
     * if he administrates the root division node he gets the role "ROLE_SUPERADMIN" and "ROLE_ADMIN", otherwise he is "ROLE_USER"
     * @param user The user, whose authorities need to be checked.
     * @return A list of the roles of the user.
     */
    private ArrayList<GrantedAuthority> getUserAuthorities(User user)
    {
        logger.debug("Checking user role");
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        List<Division> administratedDiv = divisionRepository.findByAdminUser(user);
        if(administratedDiv.isEmpty())
        {
            logger.debug("Authenticated user is ROLE_USER");
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else
        {
            logger.debug("Authenticated user is ROLE_ADMIN");
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            if(administratedDiv.stream().anyMatch(div -> div.getParent() == null))
            {
                logger.debug("Authenticated user is ROLE_SUPERADMIN");
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
            }
        }
        return authorities;
    }
}
