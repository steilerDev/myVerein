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
package de.steilerdev.myVerein.server.security;

import de.steilerdev.myVerein.server.model.rememberMeToken.RememberMeToken;
import de.steilerdev.myVerein.server.model.rememberMeToken.RememberMeTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;
import java.util.List;

/**
 * This data provider is implementing the necessary functions to enable a persistent token repository. This provider is needed to user the remember me functionality. This class stores all remember me tokens in the database.
 */
public class RememberMeTokenDataProvider implements PersistentTokenRepository
{
    @Autowired
    RememberMeTokenRepository rememberMeTokenRepository;

    private final Logger logger = LoggerFactory.getLogger(RememberMeTokenRepository.class);

    /**
     * This function stores a new remember me token in the database.
     * @param persistentRememberMeToken The new token, that needs to be stored presistent.
     */
    @Override
    public void createNewToken(PersistentRememberMeToken persistentRememberMeToken)
    {
        logger.debug("[{}] Creating new remember me token", persistentRememberMeToken.getUsername());
        rememberMeTokenRepository.save(new RememberMeToken(persistentRememberMeToken));
    }

    /**
     * This function updates an existing remember me token.
     * @param series The series of the remember me token.
     * @param tokenValue The new token value.
     * @param lastUsed The last used date of the token.
     */
    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed)
    {
        RememberMeToken token = rememberMeTokenRepository.findRememberMeTokenBySeries(series);
        if(token != null)
        {
            logger.debug("[{}] Updating remember me token of series {}", token.getUsername(),series);
            token = new RememberMeToken(token.getUsername(), series, tokenValue, lastUsed);
            rememberMeTokenRepository.save(token);
        } else
        {
            logger.warn("Unable to update remember me token of series {}", series);
        }
    }

    /**
     * Retrieves a token defined by it's series.
     * @param series The series of the token.
     * @return The token retrieved from the series.
     */
    @Override
    public PersistentRememberMeToken getTokenForSeries(String series)
    {
        logger.debug("Getting token for series {}", series);
        return rememberMeTokenRepository.findRememberMeTokenBySeries(series).toPersistentRememberMeToken();
    }

    /**
     * Removes the token for a specified user.
     * @param username The username of the user.
     */
    @Override
    public void removeUserTokens(String username)
    {
        List<RememberMeToken> tokens = rememberMeTokenRepository.findRememberMeTokenByUsername(username);
        if(tokens != null)
        {
            logger.debug("[{}] Deleting remember me token", username);
            try {
                rememberMeTokenRepository.delete(tokens);
            } catch(IllegalArgumentException e)
            {
                logger.error("[{}] Unable to delete remember me token: {}", username, e.getMessage());
            }
        } else
        {
            logger.warn("[{}] Unable to retrieve remember me", username);
        }
    }
}
