package de.steilerdev.myVerein.server.controller;

import de.steilerdev.myVerein.server.model.Division;
import de.steilerdev.myVerein.server.model.DivisionRepository;
import de.steilerdev.myVerein.server.model.User;
import de.steilerdev.myVerein.server.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/")
public class HelloController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DivisionRepository divisionRepository;

	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
		User user1 = userRepository.findByFirstName("Frank");
		user1.setEmail("frank1@steilerdev.de");
		userRepository.save(user1);
		model.addAttribute("user", user1);
		return "hello";
	}
}