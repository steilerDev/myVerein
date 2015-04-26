/**
 * Copyright (C) 2015 Frank Steiler <frank@steilerdev.de>
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
package de.steilerdev.myVerein.server.security.rest;

import de.steilerdev.myVerein.server.security.SecurityHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is used to modify the default behaviour in case of an unauthorized access to a secured resource by Spring Security within the context of the system's REST API.
 * By default a 302 would be send and redirect the user to the log in page. In the context of a REST API this behaviour is not optimal. Therefore this class is not sending a 302 but a 401 status code.
 */
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint
{
    private final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    /**
     * This function is invoked, when the user accesses a secured resource, while not being authorized.
     * @param request The user's request.
     * @param response The response send from the server.
     * @param authException The thrown authentication exception.
     * @throws IOException Used to meet signature, this implementation should not throw any IOExceptions.
     */
    @Override
    public void commence( HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException ) throws IOException
    {
        logger.warn("[{}] Unauthorized access to authentication entry point", SecurityHelper.getClientIpAddr(request));
        if(!response.isCommitted())
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}