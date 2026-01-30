package com.SecurityLockers.SecureDeliveryLockers.messaging.consumer;

import com.SecurityLockers.SecureDeliveryLockers.config.RabbitMQConfig;
import com.SecurityLockers.SecureDeliveryLockers.messaging.dto.FileUploadMessage;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.Locker;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.UserProfile;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.repository.AuthRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.profile.repository.ProfileRepository;
import com.SecurityLockers.SecureDeliveryLockers.services.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class FileUploadConsumer {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @RabbitListener(queues = RabbitMQConfig.FILE_UPLOAD_QUEUE)
    public void consumeFileUpload(FileUploadMessage fileUploadMessage) {
        try {
            log.info("Consuming file upload message: {}", fileUploadMessage.getFileName());
            
            // Convert byte array back to MultipartFile
            MultipartFile multipartFile = new MockMultipartFile(
                    fileUploadMessage.getFileName(),
                    fileUploadMessage.getFileName(),
                    fileUploadMessage.getContentType(),
                    fileUploadMessage.getFileContent()
            );
            
            // Upload to S3
            String fileUrl = s3Service.uploadFile(multipartFile);
            log.info("File uploaded to S3: {}", fileUrl);
            
            // Update entity with file URL based on callback type
            if ("LOCKER".equals(fileUploadMessage.getCallbackType())) {
                updateLockerImage(fileUploadMessage.getEntityId(), fileUrl);
            } else if ("PROFILE".equals(fileUploadMessage.getCallbackType())) {
                updateProfileImage(fileUploadMessage.getEntityId(), fileUrl);
            }
            
            log.info("File upload processed successfully: {}", fileUploadMessage.getFileName());
        } catch (Exception e) {
            log.error("Failed to process file upload for {}: {}", 
                    fileUploadMessage.getFileName(), e.getMessage(), e);
            throw new RuntimeException("Failed to process file upload message", e);
        }
    }

    private void updateLockerImage(String entityId, String imageUrl) {
        try {
            if (entityId == null || entityId.isEmpty()) {
                log.warn("Cannot update locker image: entityId is null or empty");
                return;
            }
            
            UUID lockerId = UUID.fromString(entityId);
            lockerRepository.findById(lockerId)
                    .ifPresent(locker -> {
                        locker.setLockerImage(imageUrl);
                        lockerRepository.save(locker);
                        log.info("Updated locker {} with image URL: {}", lockerId, imageUrl);
                    });
        } catch (Exception e) {
            log.error("Failed to update locker image for entity {}: {}", entityId, e.getMessage(), e);
        }
    }
    
    private void updateProfileImage(String entityId, String imageUrl) {
        try {
            if (entityId == null || entityId.isEmpty()) {
                log.warn("Cannot update profile image: entityId is null or empty");
                return;
            }
            
            Long userId = Long.parseLong(entityId);
            User user = authRepository.findById(userId).orElse(null);
            
            if (user != null && user.getUserProfile() != null) {
                UserProfile profile = user.getUserProfile();
                profile.setProfileImage(imageUrl);
                profileRepository.save(profile);
                log.info("Updated profile image for user {}: {}", userId, imageUrl);
            } else {
                log.warn("User or profile not found for userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to update profile image for entity {}: {}", entityId, e.getMessage(), e);
        }
    }
}

