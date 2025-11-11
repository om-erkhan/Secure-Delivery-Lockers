package com.SecurityLockers.SecureDeliveryLockers.modules.profile.service;

import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.UserProfile;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.repository.AuthRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.profile.dto.CreateProfileDto;
import com.SecurityLockers.SecureDeliveryLockers.modules.profile.repository.ProfileRepository;
import com.SecurityLockers.SecureDeliveryLockers.services.S3Service;
import com.SecurityLockers.SecureDeliveryLockers.utility.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
@Slf4j
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AuthRepository userRepository;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AuthUtils authUtils;

    public UserProfile createProfile(CreateProfileDto dto) throws Exception {
        log.info("---- Controller hit: /create-profile ----");
        log.info("Received file: {}", dto.getProfileImage() != null ? dto.getProfileImage().getOriginalFilename() : "null");

        User user = authUtils.getCurrentUser();
        if(user.getIsProfileCompleted()){
            throw new RuntimeException("Profile is already completed");
        }


        String imageUrl = null;

        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            imageUrl = s3Service.uploadFile(dto.getProfileImage());
        }

        UserProfile profile = UserProfile.builder()
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .profileImage(imageUrl)
                .user(user)
                .build();

        user.setIsProfileCompleted(true);

        userRepository.save(user);
        return profileRepository.save(profile);
    }
}
