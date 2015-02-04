package de.steilerdev.myVerein.server.controller;

import de.steilerdev.myVerein.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * This controller is processing all general requests.
 */
@Controller
@RequestMapping("/")
public class IndexController
{
	//A global time zone variable,should be defined later using settings
	public static final String timeZone = "";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DivisionRepository divisionRepository;

	@Autowired
	private EventRepository eventRepository;

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private GridFSRepository gridFSRepository;

	private static Logger logger = LoggerFactory.getLogger(IndexController.class);

	/**
	 * This request mapping is processing the request to view the application page.
	 * @return The path to the view for the index page.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String startApplication(Model model) {
        model.addAttribute("clubName", settingsRepository.getClubName());
		return "index";
	}

	/**
	 * This request mapping is processing the request to view the login page.
	 * @return The path to the view for the login page.
	 */
	@RequestMapping(value = "login",method = RequestMethod.GET)
	public String login(@RequestParam (required = false) String error,
                        @RequestParam (required = false) String logout,
                        @RequestParam (required = false) String cookieTheft,
                        Model model)
	{
        model.addAttribute("clubName", settingsRepository.getClubName());
        if(error != null)
        {
            logger.warn("An error occurred during log in");
        }
        if(logout != null)
        {
            logger.debug("A user successfully logged out");
        }
        if(cookieTheft != null)
        {
            logger.error("A possible cookie theft was observed!");
        }
        //createDatabaseExample();
		return "login";
	}

    @RequestMapping(value = "error", method = RequestMethod.GET)
    public String error()
    {
        logger.warn("An unexpected error was recognized, redirecting to error page.");
        return "error";
    }

    /**
     * Dummy method for a work around to disable application flooding by IntelliJ.
     * See https://youtrack.jetbrains.com/issue/IDEA-135196
     * @return OK
     */
    @RequestMapping(method = RequestMethod.HEAD, value = "*")
    public ResponseEntity<String> fuck(@RequestHeader Map<String, String> headers)
    {
        for(String headerKey: headers.keySet())
        {
            System.err.print(headerKey + ": " + headers.get(headerKey) + "; ");
        }
        System.err.print("\n");
        return new ResponseEntity<>("No", HttpStatus.OK);
    }

    /**
     * This function is serving the favicon of the server
     * @return
     */
    @RequestMapping("favicon.ico")
    String favicon()
    {
        return "forward:/resources/images/favicon.ico";
    }

	/**
	 * This function is clearing the current database and creates a new set of test data.
	 */
	private void createDatabaseExample()
	{
		logger.debug("Deleting old database and reloading database example.");
		userRepository.deleteAll();
		divisionRepository.deleteAll();
		eventRepository.deleteAll();

		User user1 = new User("Frank", "Steiler", "frank@steiler.eu", "asdf");
		user1.setBirthday(LocalDate.of(1994, 6, 28));
		user1.setActiveSince(LocalDate.of(2000, 1, 1));
		user1.setIban("DE46500700100927353010");
		user1.setBic("BYLADEM1001");
		user1.setCity("Stuttgart");
		user1.setZipCode("70190");
		user1.setStreetNumber("27");
		user1.setStreet("Metzstraße");
		user1.setCountry("Germany");
		user1.setGender(User.Gender.MALE);
		User user2 = new User("John", "Doe", "john@doe.com", "asdf");
		user2.setActiveSince(LocalDate.of(1999,1,1));
		user2.setPassiveSince(LocalDate.of(2000,6,1));
		User user3 = new User("Peter", "Enis", "peter@enis.com", "asdf");
		User user4 = new User("Luke", "Skywalker", "luke@skywalker.com", "asdf");
		user4.setGender(User.Gender.MALE);
		User user5 = new User("Marty", "McFly", "marty@mcfly.com", "asdf");
		User user6 = new User("Tammo", "Schwindt", "tammo@tammon.de", "asdf");

		userRepository.save(user1);
		userRepository.save(user2);
		userRepository.save(user3);
		userRepository.save(user4);
		userRepository.save(user5);
		userRepository.save(user6);

		Division div1 = new Division("myVerein", null, user1, null);
		Division div2 = new Division("Rugby", null, user2, div1);
		Division div3 = new Division("Soccer", null, null, div1);
		Division div4 = new Division("Rugby - 1st team", null, user2, div2);
		Division div5 = new Division("Rugby - 2nd team", null, user3, div2);

		divisionRepository.save(div1);
		divisionRepository.save(div2);
		divisionRepository.save(div3);
		divisionRepository.save(div4);
		divisionRepository.save(div5);

		user2.addDivision(div2);
		user2.addDivision(div4);
		user3.addDivision(div2);
		user4.addDivision(div3);
		user5.addDivision(div2);
		user5.addDivision(div4);

		userRepository.save(user1);
		userRepository.save(user2);
		userRepository.save(user3);
		userRepository.save(user4);
		userRepository.save(user5);

		Event event1 = new Event();
		event1.setStartDateTime(LocalDateTime.of(2015, 1, 20, 13, 00));
		event1.setEndDateTime(LocalDateTime.of(2015, 1, 20, 14, 00));
		event1.setName("Super Event 1");
		event1.addDivision(div2);
		event1.setEventAdmin(user1);
		event1.updateMultiDate();

		Event event2 = new Event();
		event2.setStartDateTime(LocalDateTime.of(2015, 1, 20, 13, 00));
		event2.setEndDateTime(LocalDateTime.of(2015, 1, 21, 13, 00));
		event2.setName("Super Event 2");
		event2.addDivision(div3);
		event2.setEventAdmin(user4);
		event2.updateMultiDate();

		Event event3 = new Event();
		event3.setStartDateTime(LocalDateTime.of(2015, 1, 21, 13, 00));
		event3.setEndDateTime(LocalDateTime.of(2015, 1, 21, 13, 05));
		event3.setName("Super Event 3");
		event3.addDivision(div1);
		event3.setEventAdmin(user1);
		event3.updateMultiDate();

		Event event4 = new Event();
		event4.setStartDateTime(LocalDateTime.of(2015, 1, 11, 13, 00));
		event4.setEndDateTime(LocalDateTime.of(2015, 1, 15, 13, 05));
		event4.setName("Super Event 4");
		event4.addDivision(div1);
		event4.setEventAdmin(user2);
		event4.updateMultiDate();

		eventRepository.save(event1);
		eventRepository.save(event2);
		eventRepository.save(event3);
		eventRepository.save(event4);
	}
}