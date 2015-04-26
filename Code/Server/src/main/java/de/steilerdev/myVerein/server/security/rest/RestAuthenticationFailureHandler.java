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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class is used to modify the default behaviour in case of a authentication failure within the context of the system's REST API.
 */
public class RestAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
    private final Logger logger = LoggerFactory.getLogger(RestAuthenticationFailureHandler.class);

    /**
     * This function is invoked if a user's authentication went wrong. In that case a simple 401 is send.
     * @param request The user's request.
     * @param response The server's response.
     * @param exception The authentication exception.
     * @throws IOException Used to meet signature, this implementation should not throw any IOExceptions.
     * @throws ServletException Used to meet signature, this implementation should not throw any ServletExceptions.
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException
    {
        logger.warn("[{}] Authentication failed: {}", SecurityHelper.getClientIpAddr(request), exception.getMessage());
        if(!response.isCommitted())
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed: " + exception.getMessage());
        }
    }
}
