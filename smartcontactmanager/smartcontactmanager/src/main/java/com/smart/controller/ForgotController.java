package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
     Random random=new Random(1000);

     @Autowired
     private EmailService emailService;
     @Autowired
     private UserRepository userRepository;
     @Autowired
     private BCryptPasswordEncoder bCryptPasswordEncoder;



    //email.id form open handler

    @RequestMapping("/forgot")
    public String openEmailForm(){
        return "forgot_email_form";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email")String email,HttpSession session ){
        System.out.println("EMAIL "+email);

        //Generation otp of 4 digit
       

        int otp=random.nextInt(999999);
        System.out.println("OTP "+otp);

        //code for sending email
        String subject="OTP From Smart Contact Manager";
        String message=""+
                        "<div style='border:1px solid #e2e2e2; padding:20px'>"+
                        "<h1>"+
                        "OTP is "+
                        "<b>"+otp+
                        "</b>"+
                        "</h1>"+
                        "</div>";
        String to=email;

        boolean sent=emailService.sendEmail(message, subject, to);

        if (sent) {
            session.setAttribute("myotp",otp);
            session.setAttribute("email", email);
            return "verify_otp";
        }else{
            session.setAttribute("message","Please check your email ID..");
        }


        


        return "forgot_email_form";
    }

    //verify otp
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp")int otp,HttpSession session){

        int myOtp=(int)session.getAttribute("myotp");
        String email=(String)session.getAttribute("email");
        
        if(myOtp==otp){
            //password change form
            User user= this.userRepository.getUserByUserName(email);
            if(user==null){
                //send error message
                session.setAttribute("message", "User does not exists with this email id!!");
                return "forgot_email_form";
            }else{
                //send change password form
            }

            return "password_change_form";
        }else{

            session.setAttribute("message", "You have entered wrong OTP");
            return "verify_otp";
        }


    }

    //change password

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword")String newpassword,HttpSession session){

        //fetching email
        String email=(String)session.getAttribute("email");
        //fetching user
        User user=this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
        this.userRepository.save(user);
        
        return "redirect:/log?change=Password Changed Successfully";
       
    }
    
}
