package de.steilerdev.myVerein.server.controller;

import de.steilerdev.myVerein.server.model.Division;
import de.steilerdev.myVerein.server.model.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.model.UserRepository;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Time;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This controller is processing all general requests.
 */
@Controller
@RequestMapping("/")
public class IndexController
{
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DivisionRepository divisionRepository;

	private static Logger logger = LoggerFactory.getLogger(IndexController.class);

	/**
	 * This request mapping is processing the request to view the index page.
	 * @param model The model handed over to the view.
	 * @param currentUser The currently logged in user.
	 * @return The path to the view for the index page.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model, @CurrentUser User currentUser) {
		model.addAttribute("user", currentUser);
		return "index";
	}

	/**
	 * This request mapping is processing the request to view the login page.
	 * @return The path to the view for the login page.
	 */
	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String login()
	{
		//createDatabaseExample();
		return "login";
	}

	/**
	 * This function is clearing the current database and creates a new set of test data.
	 */
	private void createDatabaseExample()
	{
		logger.debug("Deleting old database and reloading database example.");
		userRepository.deleteAll();
		divisionRepository.deleteAll();

		User user1 = new User("Frank", "Steiler", "frank@steiler.eu", "asdf");
		user1.setBirthday(LocalDate.of(1994, 6, 28));
		user1.setMemberSince(LocalDate.of(2000, 1, 1));
		User user2 = new User("John", "Doe", "john@doe.com", "asdf");
		User user3 = new User("Peter", "Enis", "peter@enis.com", "asdf");
		User user4 = new User("Luke", "Skywalker", "luke@skywalker.com", "asdf");
		user4.addPrivateInformation("IBAN", "ABCDEFG");
		user4.addPublicInformation("Gender", "Male");
		User user5 = new User("Marty", "McFly", "marty@mcfly.com", "asdf");

		userRepository.save(user1);
		userRepository.save(user2);
		userRepository.save(user3);
		userRepository.save(user4);
		userRepository.save(user5);

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
	}
}