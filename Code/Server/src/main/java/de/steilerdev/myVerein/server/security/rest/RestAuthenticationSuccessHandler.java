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

import de.steilerdev.myVerein.server.model.Settings;
import de.steilerdev.myVerein.server.model.SettingsRepository;
import de.steilerdev.myVerein.server.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler
{

    @Autowired
    SettingsRepository settingsRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException
    {
        clearAuthenticationAttributes(request);
        if(!response.isCommitted())
        {
            Settings settings =  Settings.loadSettings(settingsRepository);
            response.setHeader("System-ID", settings.getId());
            response.setHeader("System-Version", settings.getSystemVersion());
            response.setHeader("User-ID", ((User)authentication.getPrincipal()).getId());
            response.sendError(HttpServletResponse.SC_OK, "Successfully logged in");
        }
    }
}