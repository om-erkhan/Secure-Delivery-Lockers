package com.SecurityLockers.SecureDeliveryLockers.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage implements Serializable {
    private String to;
    private String subject;
    private String body;
    private String userOtp;
    private String deliveryOtp;
    private EmailType emailType;

    public enum EmailType {
        OTP_REGISTRATION,
        OTP_LOGIN,
        RESERVATION_OTP,
        PARCEL_DELIVERED,
        PARCEL_PICKED_UP,
        RESERVATION_EXPIRED,
        RESERVATION_EXPIRING_SOON,
        REMINDER,
        RENEW_OTP_REQUIRED
    }

    // Helper methods for different email types
    public static EmailMessage createOtpEmail(String to, String otp) {
        return EmailMessage.builder()
                .to(to)
                .subject("Your OTP Code")
                .body("Your OTP code for registration is: " + otp + "\nThis code will expire in 5 minutes.")
                .emailType(EmailType.OTP_REGISTRATION)
                .build();
    }

    public static EmailMessage createReservationOtpEmail(String to, String userOtp, String deliveryOtp) {
        return EmailMessage.builder()
                .to(to)
                .subject("Your OTP Code")
                .body("Your OTP code is: " + userOtp + 
                      "\nDelivery Person OTP is: " + deliveryOtp + 
                      "\nBoth of these codes will be expired in 24 Hours..")
                .userOtp(userOtp)
                .deliveryOtp(deliveryOtp)
                .emailType(EmailType.RESERVATION_OTP)
                .build();
    }

    public static EmailMessage createParcelDeliveredEmail(String to) {
        return EmailMessage.builder()
                .to(to)
                .subject("Locker Update")
                .body("Your Parcel has been placed successfully")
                .emailType(EmailType.PARCEL_DELIVERED)
                .build();
    }

    public static EmailMessage createParcelPickedUpEmail(String to) {
        return EmailMessage.builder()
                .to(to)
                .subject("Locker Update")
                .body("Your Parcel has been picked successfully, don't forget to rate us :P")
                .emailType(EmailType.PARCEL_PICKED_UP)
                .build();
    }

    public static EmailMessage createReservationExpiredEmail(String to) {
        return EmailMessage.builder()
                .to(to)
                .subject("Reservation Expired")
                .body("Your locker reservation has expired. Please create a new reservation.")
                .emailType(EmailType.RESERVATION_EXPIRED)
                .build();
    }

    public static EmailMessage createReservationExpiringSoonEmail(String to) {
        return EmailMessage.builder()
                .to(to)
                .subject("Reminder: Your Locker Reservation Expires Soon")
                .body("Your locker reservation will expire in 1 hour. Please collect your parcel or renew your reservation from the app.")
                .emailType(EmailType.RESERVATION_EXPIRING_SOON)
                .build();
    }

    public static EmailMessage createRenewOtpRequiredEmail(String to) {
        return EmailMessage.builder()
                .to(to)
                .subject("Reservation Expired - Renew OTP Required")
                .body("Your locker reservation has expired. Your OTP is no longer valid. " +
                      "Please renew your OTP from the app to access your locker.")
                .emailType(EmailType.RENEW_OTP_REQUIRED)
                .build();
    }
}

