package de.steilerdev.myVerein.server.controller;

import de.steilerdev.myVerein.server.model.Division;
import de.steilerdev.myVerein.server.model.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.model.UserRepository;
import de.steilerdev.myVerein.server.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;


@Controller
@RequestMapping("/")
public class HelloController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DivisionRepository divisionRepository;

	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model, @CurrentUser User user) {
		user.setPassword("asdf");
		userRepository.save(user);
		model.addAttribute("user", user);
		return "hello";
	}

	@RequestMapping(value = "login", method = RequestMethod.GET)
	public String login()
	{
		return "login";
	}
}