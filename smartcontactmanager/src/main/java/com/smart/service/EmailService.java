package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    String message="This is email sending page";
    String subject="for testing purpose";
    String to="gauravlohar3240@gmail.com";
    

    public static boolean sendEmail(String message,String subject,String to){
        String from="rushikeshlohar3240@gmail.com";
        // variable for email
        String host="smtp.gmail.com";

        //get the system properties
        Properties properties=System.getProperties();
        System.out.println("Properties "+properties);

        //setting important information properties to object
        //host

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", true);
        properties.put("mail.smtp.auth", true);

        //step 1: to get session object

        Session session=Session.getInstance(properties,new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
               
                return new PasswordAuthentication("rushikeshlohar3240@gmail.com", "chmq pzot rokz nsjk");
            }
            
        });
        session.setDebug(true);

        // step 2: compose the msg [text,multimedia]
        MimeMessage m= new MimeMessage(session);


        try {
            //from email
            m.setFrom(from);

            //adding recipinet message
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message
            m.setSubject(subject);

            //adding text to subject
           // m.setText(message);
           m.setContent(message,"text/html");

            //send
            //send message using transport class
            Transport.send(m);
            System.out.println("Sent successfully...");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            
            return false;
        }
    }
}
