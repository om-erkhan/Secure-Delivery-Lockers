package com.SecurityLockers.SecureDeliveryLockers.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendMail(String to, String userOtp, String deliveryManOtp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Otp Code");
        message.setText("Your OTP code is: " + userOtp + "\nDelivery Person Otp is: " + deliveryManOtp + "\nBoth of these codes will be expired in 24 Hours..");
        mailSender.send(message);
    }

    public boolean sendMail(String to, String userOtp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Otp Code");
        message.setText("Your OTP code for registration is: " + userOtp + "\nThis code will expire in 5 minutes.");
        mailSender.send(message);
        return true;
    }



}
