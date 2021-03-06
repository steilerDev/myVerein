package de.steilerdev.myVerein.server.controller;

import de.steilerdev.myVerein.server.model.settings.Settings;
import de.steilerdev.myVerein.server.model.settings.SettingsHelper;
import de.steilerdev.myVerein.server.model.settings.SettingsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * This controller is processing all general requests.
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @Autowired
    private SettingsRepository settingsRepository;

    private final Logger logger = LoggerFactory.getLogger(IndexController.class);

    /**
     * The default club name shown, if none is defined
     */
    private final String defaultClubName = "myVerein";

    /**
     * This request mapping is processing the request to view the application page and retrieves the club name.
     * @param model The model, handed over to the template engine rendering the view.
     * @return The path to the view for the index page, together with the club name as parameter.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String startApplication(Model model)
    {
        model.addAttribute("clubName", getClubName());
        logger.debug("Returning index view");
        return "index";
    }

    /**
     * This request mapping is processing the request to view the login page. If the initial flag is set within the settings file or there is no settings document available in the database, the initial configuration page is returned.
     * @param error This parameter is present if a login error occurred.
     * @param logout This parameter is present if a user logged out of the application and got redirected to this page.
     * @param cookieTheft This parameter is present if a cookieTheft exception was thrown, which means that the rememberMeCookie of the user might have been compromised.
     * @param model The model, handed over to the template engine rendering the view.
     * @return The path to the view for the login or initial configuration page.
     */
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login(@RequestParam(required = false) String error, @RequestParam(required = false) String logout, @RequestParam(required = false) String cookieTheft, Model model)
    {
        logger.trace("Getting login page");
        if (SettingsHelper.initialSetupNeeded(settingsRepository))
        {
            logger.warn("Starting initial setup");
            return "init";
        } else
        {
            model.addAttribute("clubName", getClubName());
            if (error != null)
            {
                logger.warn("An error occurred during log in");
            }
            if (logout != null)
            {
                logger.debug("An user successfully logged out");
            }
            if (cookieTheft != null)
            {
                logger.error("A possible cookie theft was observed!");
            }
            logger.info("Returning login view");
            return "login";
        }
    }

    /**
     * This process mapping is used for error handling and is presented if the application server encountered an uncaught exception.
     * @param noDB If a database timeout exception was thrown this parameter is present.
     * @param pageNotFound If a 404 or 405 HTTP response code would have been send to the user this parameter is present.
     * @return The path to the view for the error page.
     */
    @RequestMapping(value = "error", method = RequestMethod.GET)
    public String error(@RequestParam(required = false) String noDB, @RequestParam(required = false) String pageNotFound)
    {
        if(noDB != null)
        {
            logger.warn("An exception was recognized: Unable to access database. Redirecting to error page.");
        } else if(pageNotFound != null)
        {
            logger.warn("The requested page was not found.");
        } else
        {
            logger.warn("An unexpected exception was recognized. Redirecting to error page.");
        }
        return "error";
    }

    /**
     * Dummy method for a work around to disable application flooding by IntelliJ. See
     * https://youtrack.jetbrains.com/issue/IDEA-135196
     * @return OK
     */
    @RequestMapping(method = RequestMethod.HEAD, value = "*")
    public ResponseEntity<String> workAround(@RequestHeader Map<String, String> headers)
    {
        for (String headerKey : headers.keySet())
        {
            System.err.print(headerKey + ": " + headers.get(headerKey) + "; ");
        }
        System.err.print("\n");
        return new ResponseEntity<>("No", HttpStatus.OK);
    }

    /**
     * This function tries to retrieve the defined club name from the database. If the club name is not available a default one is used.
     * @return The club name.
     */
    private String getClubName()
    {
        logger.trace("Gathering club name");
        Settings currentSettings = SettingsHelper.loadSettings(settingsRepository);
        String clubName;
        if(currentSettings == null || (clubName = currentSettings.getClubName()) == null || clubName.isEmpty())
        {
            logger.warn("Unable to retrieve club name, using default name");
            clubName = defaultClubName;
        }
        return clubName;
    }
}