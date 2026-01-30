package com.SecurityLockers.SecureDeliveryLockers.messaging.producer;

import com.SecurityLockers.SecureDeliveryLockers.config.RabbitMQConfig;
import com.SecurityLockers.SecureDeliveryLockers.messaging.dto.FileUploadMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class FileUploadProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendFileUploadMessage(FileUploadMessage fileUploadMessage) {
        try {
            log.info("Sending file upload message to queue: {}", fileUploadMessage.getFileName());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.FILE_UPLOAD_EXCHANGE,
                    RabbitMQConfig.FILE_UPLOAD_ROUTING_KEY,
                    fileUploadMessage
            );
            log.info("File upload message sent successfully to queue: {}", fileUploadMessage.getFileName());
        } catch (Exception e) {
            log.error("Failed to send file upload message to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to queue file upload message", e);
        }
    }

    public void queueLockerImageUpload(MultipartFile file, UUID lockerId) throws IOException {
        FileUploadMessage message = FileUploadMessage.createLockerImageUpload(
                file.getOriginalFilename(),
                file.getBytes(),
                file.getContentType(),
                lockerId.toString()
        );
        sendFileUploadMessage(message);
    }

    public void queueProfileImageUpload(MultipartFile file, Long userId) throws IOException {
        FileUploadMessage message = FileUploadMessage.createProfileImageUpload(
                file.getOriginalFilename(),
                file.getBytes(),
                file.getContentType(),
                userId
        );
        sendFileUploadMessage(message);
    }
}

