package com.SecurityLockers.SecureDeliveryLockers.messaging.producer;

import com.SecurityLockers.SecureDeliveryLockers.config.RabbitMQConfig;
import com.SecurityLockers.SecureDeliveryLockers.messaging.dto.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmail(EmailMessage emailMessage) {
        try {
            log.info("Sending email message to queue: {}", emailMessage.getTo());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailMessage
            );
            log.info("Email message sent successfully to queue for: {}", emailMessage.getTo());
        } catch (Exception e) {
            log.error("Failed to send email message to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to queue email message", e);
        }
    }

    public void sendOtpEmail(String to, String otp) {
        EmailMessage emailMessage = EmailMessage.createOtpEmail(to, otp);
        sendEmail(emailMessage);
    }

    public void sendReservationOtpEmail(String to, String userOtp, String deliveryOtp) {
        EmailMessage emailMessage = EmailMessage.createReservationOtpEmail(to, userOtp, deliveryOtp);
        sendEmail(emailMessage);
    }

    public void sendParcelDeliveredEmail(String to) {
        EmailMessage emailMessage = EmailMessage.createParcelDeliveredEmail(to);
        sendEmail(emailMessage);
    }

    public void sendParcelPickedUpEmail(String to) {
        EmailMessage emailMessage = EmailMessage.createParcelPickedUpEmail(to);
        sendEmail(emailMessage);
    }

    public void sendReservationExpiredEmail(String to) {
        EmailMessage emailMessage = EmailMessage.createReservationExpiredEmail(to);
        sendEmail(emailMessage);
    }

    public void sendReservationExpiringSoonEmail(String to) {
        EmailMessage emailMessage = EmailMessage.createReservationExpiringSoonEmail(to);
        sendEmail(emailMessage);
    }

    public void sendRenewOtpRequiredEmail(String to) {
        EmailMessage emailMessage = EmailMessage.createRenewOtpRequiredEmail(to);
        sendEmail(emailMessage);
    }
}

