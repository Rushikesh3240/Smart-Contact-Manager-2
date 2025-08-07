package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.springframework.validation.BindingResult;


import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;	
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/")
	public String home(Model model) {

		model.addAttribute("title", "Home-Smart Contact Manager");

		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {

		model.addAttribute("title", "About-Smart Contact Manager");

		return "about";
	}
	
	@GetMapping("/signup")
	public String signup1(Model model) {
		model.addAttribute("title","Register-Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
		
	}
	
	//this handler for registring user
	//@RequestMapping(value="/do_register",method = RequestMethod.POST)
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user")User user 
			,BindingResult r,@RequestParam(value = "agreement",defaultValue = "false")
			boolean agreement, Model model,HttpSession session) {
	
		try {
			if(!agreement) {
				System.out.println("You have not agreed terms and conditions");
				throw new Exception("You have not agreed terms and conditions");
			}
			
			if(r.hasErrors()) {
				System.out.println("Error" +r.toString());
				model.addAttribute("user",user);
				System.out.println(r);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.println("USER"+user);
			
			User result= this.userRepository.save(user);
			
			model.addAttribute("user",new User()) ;
			

			session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
			return "signup";
			
			
		} catch (Exception e) {
			e.printStackTrace();
			
			model.addAttribute("user",user);
			
			session.setAttribute("message", new Message("Something went wrong"+ e.getMessage(), "alert-danger"));
			return "signup"; 
		}
		
	}
	
	@GetMapping("/log")
	public String login(Model model) {
		model.addAttribute("title","Login Page");
		
		return "login";
	}
	
	

}
