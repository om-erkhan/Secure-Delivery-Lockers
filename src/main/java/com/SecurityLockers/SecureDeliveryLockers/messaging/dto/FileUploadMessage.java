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
public class FileUploadMessage implements Serializable {
    private String fileName;
    private byte[] fileContent;
    private String contentType;
    private UploadType uploadType;
    private String entityId; // For linking to locker/profile after upload (UUID as string for locker, Long as string for profile)
    private String callbackType; // "LOCKER" or "PROFILE"
    
    public enum UploadType {
        LOCKER_IMAGE,
        PROFILE_IMAGE
    }

    public static FileUploadMessage createLockerImageUpload(String fileName, byte[] fileContent, String contentType, String lockerId) {
        return FileUploadMessage.builder()
                .fileName(fileName)
                .fileContent(fileContent)
                .contentType(contentType)
                .uploadType(UploadType.LOCKER_IMAGE)
                .entityId(lockerId)
                .callbackType("LOCKER")
                .build();
    }

    public static FileUploadMessage createProfileImageUpload(String fileName, byte[] fileContent, String contentType, Long userId) {
        return FileUploadMessage.builder()
                .fileName(fileName)
                .fileContent(fileContent)
                .contentType(contentType)
                .uploadType(UploadType.PROFILE_IMAGE)
                .entityId(String.valueOf(userId))
                .callbackType("PROFILE")
                .build();
    }
}

