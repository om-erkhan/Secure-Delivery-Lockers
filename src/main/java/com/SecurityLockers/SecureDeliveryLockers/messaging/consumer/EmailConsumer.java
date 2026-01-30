package com.SecurityLockers.SecureDeliveryLockers.messaging.consumer;

import com.SecurityLockers.SecureDeliveryLockers.config.RabbitMQConfig;
import com.SecurityLockers.SecureDeliveryLockers.messaging.dto.EmailMessage;
import com.SecurityLockers.SecureDeliveryLockers.utility.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailConsumer {

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeEmail(EmailMessage emailMessage) {
        try {
            log.info("Consuming email message for: {}", emailMessage.getTo());
            
            // Handle different email types
            switch (emailMessage.getEmailType()) {
                case OTP_REGISTRATION:
                case OTP_LOGIN:
                    emailService.sendMail(emailMessage.getTo(), emailMessage.getBody().split(":")[1].trim());
                    break;
                    
                case RESERVATION_OTP:
                    emailService.sendMail(
                            emailMessage.getTo(),
                            emailMessage.getUserOtp(),
                            emailMessage.getDeliveryOtp()
                    );
                    break;
                    
                case PARCEL_DELIVERED:
                case PARCEL_PICKED_UP:
                case RESERVATION_EXPIRED:
                case RESERVATION_EXPIRING_SOON:
                case RENEW_OTP_REQUIRED:
                case REMINDER:
                    emailService.sendMail(
                            emailMessage.getTo(),
                            emailMessage.getSubject(),
                            emailMessage.getBody(),
                            ""
                    );
                    break;
                    
                default:
                    // Generic email sending
                    emailService.sendMail(
                            emailMessage.getTo(),
                            emailMessage.getSubject(),
                            emailMessage.getBody(),
                            ""
                    );
            }
            
            log.info("Email sent successfully to: {}", emailMessage.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", emailMessage.getTo(), e.getMessage(), e);
            // In production, you might want to send to a dead-letter queue
            throw new RuntimeException("Failed to process email message", e);
        }
    }
}

