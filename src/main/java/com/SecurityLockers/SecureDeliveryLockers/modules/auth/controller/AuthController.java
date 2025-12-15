package com.SecurityLockers.SecureDeliveryLockers.modules.auth.controller;


import com.SecurityLockers.SecureDeliveryLockers.modules.auth.dto.RegisterRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.service.AuthService;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.AuthResponse;
import com.SecurityLockers.SecureDeliveryLockers.utility.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public ResponseEntity <?> login(@RequestBody RegisterRequestDTO request){
        AuthResponse res =  authService.login(request);
        Map<String,Object> data= new HashMap<>();
        data.put("status", res.getStatus());
        data.put("token", res.getToken());
        return ResponseBuilder.success( data  , res.getMessage());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");
        String message = authService.verifyOtp(email, otp);
        return ResponseBuilder.success(message, "Otp verified Successfully.");
    }

}
