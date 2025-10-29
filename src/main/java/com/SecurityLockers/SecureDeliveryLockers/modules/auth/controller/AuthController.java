package com.SecurityLockers.SecureDeliveryLockers.modules.auth.controller;


import com.SecurityLockers.SecureDeliveryLockers.modules.auth.dto.RegisterRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.service.AuthService;
import com.SecurityLockers.SecureDeliveryLockers.utility.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/register")
    public ResponseEntity <?> registerUser(@RequestBody RegisterRequestDTO request){
       User res =  authService.register(request);
        return ResponseBuilder.success(res , "Otp is sent to your Mail, please verify to proceed.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        String message = authService.verifyOtp(email, otp);
        return ResponseBuilder.success(message, "Otp verified Successfully.");
    }

}
