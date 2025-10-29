package com.SecurityLockers.SecureDeliveryLockers.modules.profile.controller;

import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.UserProfile;
import com.SecurityLockers.SecureDeliveryLockers.modules.profile.dto.CreateProfileDto;
import com.SecurityLockers.SecureDeliveryLockers.modules.profile.service.ProfileService;
import com.SecurityLockers.SecureDeliveryLockers.utility.ResponseBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

 @Slf4j
@RestController
@RequestMapping("/api/profile")
public class ProfileController {


    @Autowired
    private ProfileService profileService;

    @PostMapping(value = "/create-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProfile(@ModelAttribute CreateProfileDto dto) {
        try {
            UserProfile profile = profileService.createProfile(dto);
            return ResponseBuilder.success(profile, "Profile created successfully!");
        } catch (Exception e) {
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }




}
