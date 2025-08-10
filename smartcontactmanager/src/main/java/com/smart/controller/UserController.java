package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	//method adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
	   String name= principal.getName();
	   System.out.println("USERNAME "+name);
	   	
	   //get the user using username(email)
	   
	   User user=userRepository.getUserByUserName(name);
	   System.out.println("USER "+name);
	   model.addAttribute("user",user);

	   
	}
	
	//dashboard home handler
	@GetMapping("/index")
	public String dashboard(Model model ,Principal principal) {
		
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	// Adding Contacts Handler
	 @GetMapping("/adding")
	 public String addContact(Model model){
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact";
	 }

	//  Processing add contact form handler

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file, Principal principal,HttpSession session){
		try{
		String name= principal.getName();
		User user= this.userRepository.getUserByUserName(name);
		

		//Proccessing and uploading file

		if(file.isEmpty()){
			//if the file is empty then try our message
			System.out.println("File is Empty");
			contact.setImage("contact.png");
		}
		else{
			//file the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
		 	File saveFile=  new ClassPathResource("static/image").getFile();

			Path path=	Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			System.out.println("Image is uploaded");
		}

		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		System.out.println("DATA "+contact);
		System.out.println("Added to database");

		//Message Success
		
		session.setAttribute("message", new Message("Your Contact is Added !! Add More","success"));
		

		}catch(Exception e){
			System.out.println("ERROR "+e);
			e.printStackTrace();
			//error Message
			
			
			session.setAttribute("message", new Message("Something went Wrong !! Try Again", "danger"));
		}

		return "normal/add_contact";
	}

	//show contacts handler
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page, Model model,Principal principal){
		model.addAttribute("title", "Show User Contacts");

		String userName= principal.getName();
		User user=this.userRepository.getUserByUserName(userName);

		Pageable  pageable=PageRequest.of(page, 5);

		Page<Contact>contacts= this.contactRepository.findContactByUser(user.getId(),pageable);
		 model.addAttribute("contacts", contacts);
		 model.addAttribute("currentPage", page);
		 model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}


	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId ,Model model,Principal principal){
		System.out.println("CID"+cId);

		Optional<Contact>contactOptional= this.contactRepository.findById(cId);
		Contact contact=contactOptional.get();

		String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);

		if(user.getId()==contact.getUser().getId())
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		

		return "normal/contact_details";
	}

	// delete contact handler
	@RequestMapping("/delete-contact/{cid}")
public String deleteContact(@PathVariable("cid") Integer cId, Principal principal, HttpSession session) {
    try {
        // Get the logged-in user
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        // Get the contact
        Optional<Contact> optionalContact = this.contactRepository.findById(cId);

        if (optionalContact.isPresent()) {
            Contact contact = optionalContact.get();

            // Check if the contact belongs to the current user
            if (contact.getUser().getId()==(user.getId())) {

				String imageName = contact.getImage();

					if (!imageName.equals("contact.png")) {
						File deleteFile = new ClassPathResource("static/image").getFile();
						File file1 = new File(deleteFile, imageName);
						file1.delete(); // deletes the file
					}
					contact.setUser(null);
                // Remove the contact
                this.contactRepository.delete(contact);

                session.setAttribute("message", new Message("Contact deleted successfully.", "success"));
            } else {
                session.setAttribute("message", new Message("You are not authorized to delete this contact.", "danger"));
            }

        } else {
            session.setAttribute("message", new Message("Contact not found.", "danger"));
        }

    } catch (Exception e) {
        e.printStackTrace();
        session.setAttribute("message", new Message("Something went wrong while deleting the contact.", "danger"));
    }

    return "redirect:/user/show-contacts/0";
}

	//open update from handler		
			@PostMapping("update-contact/{cid}")
			public String updateForm(@PathVariable("cid")Integer cid,Model model ){

				model.addAttribute("title", "Update contact");
				Contact contact=this.contactRepository.findById(cid).get();
				model.addAttribute("contact", contact);
				return "normal/update_from";
			}


	//update contact handler
	@RequestMapping(value = "/process-update" ,method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file ,Model model,HttpSession session ,Principal principal){

		try {
			//old contact details
			Contact oldContact=this.contactRepository.findById(contact.getcId()).get();


			//image...
			if (!file.isEmpty()) {
				//file work
				//rewrite

				//delete old photo
				File deleteFile=new ClassPathResource("static/image").getFile();
				File file2=new File(deleteFile, oldContact.getImage());
				file2.delete();

				//upload new file	
				File saveFile=new ClassPathResource("static/image").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath() + File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());


				
			}else{
				contact.setImage(oldContact.getImage());
			}


			// getting id
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact is updated", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		System.out.println("CONTACT NAME" +contact.getName());

		return "redirect:/user/"+contact.getcId()+"/contact";
	}

	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model){

		model.addAttribute("title", "Your Profile");
		return "normal/profile";
	}

	//open setting handler
	@GetMapping("/settings")
	public String openSetting(){

		return "normal/settings";
	}

	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam ("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,HttpSession session){
		
		System.out.println("OLD Password "+oldPassword);
		System.out.println("NEW Password "+newPassword);

		String userName=principal.getName();
		User currentUser=this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {

			//change the pass
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password is Successfully Changed...", "success"));

			
		}
		else{
			//error
			session.setAttribute("message", new Message("Please correct your old password", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}

	//creating order for payment

	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object>data) throws Exception{

		//System.out.println("Order function executed");
		System.out.println(data);

		int amt=Integer.parseInt(data.get("amount").toString());
		
	    var client=	new RazorpayClient("rzp_test_JJoDMUNiF1IeDW", "pofLDUlxU1OpnOeQXs67BNGZ");

		JSONObject object=new JSONObject();
		object.put("amount", amt*100);
		object.put("currency", "INR");
		object.put("receipt", "txn48938");

		//creating new order
		Order order=client.orders.create(object);
		System.out.println(order);
		return order.toString();
	}
}
















