package com.SecurityLockers.SecureDeliveryLockers.modules.profile.dto;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProfileDto {
    private String fullName;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private MultipartFile profileImage;
    private Long userId;
}