package com.charity.management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendDonationConfirmation(String email, String name, String charityActionTitle, String amount) {
        String subject = "Donation Confirmation";
        String text = "Dear " + name + ",\n\n" +
                "Thank you for your donation of " + amount + " to \"" + charityActionTitle + "\".\n\n" +
                "Your contribution will help make a difference.\n\n" +
                "Best regards,\n" +
                "Charity Management Team";
        
        sendEmail(email, subject, text);
    }

    public void sendParticipationConfirmation(String email, String name, String charityActionTitle, String role) {
        String subject = "Participation Request Confirmation";
        String text = "Dear " + name + ",\n\n" +
                "Your request to participate as a " + role + " in \"" + charityActionTitle + "\" has been received.\n\n" +
                "We will review your request and get back to you soon.\n\n" +
                "Best regards,\n" +
                "Charity Management Team";
        
        sendEmail(email, subject, text);
    }

    public void sendParticipationStatusUpdate(String email, String name, String charityActionTitle, String status) {
        String subject = "Participation Status Update";
        String text = "Dear " + name + ",\n\n" +
                "Your participation request for \"" + charityActionTitle + "\" has been " + status.toLowerCase() + ".\n\n" +
                "Best regards,\n" +
                "Charity Management Team";
        
        sendEmail(email, subject, text);
    }

    public void sendOrganizationStatusUpdate(String email, String organizationName, String status) {
        String subject = "Organization Status Update";
        String text = "Dear Admin,\n\n" +
                "Your organization \"" + organizationName + "\" has been " + status.toLowerCase() + ".\n\n" +
                "Best regards,\n" +
                "Charity Management Team";
        
        sendEmail(email, subject, text);
    }

    public void sendCharityActionCreationNotification(String email, String charityActionTitle) {
        String subject = "New Charity Action Created";
        String text = "Dear Admin,\n\n" +
                "A new charity action \"" + charityActionTitle + "\" has been created and is pending review.\n\n" +
                "Best regards,\n" +
                "Charity Management Team";
        
        sendEmail(email, subject, text);
    }
}
