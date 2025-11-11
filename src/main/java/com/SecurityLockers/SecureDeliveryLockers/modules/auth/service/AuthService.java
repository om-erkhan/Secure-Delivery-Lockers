package com.SecurityLockers.SecureDeliveryLockers.modules.auth.service;

import com.SecurityLockers.SecureDeliveryLockers.modules.auth.dto.RegisterRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.repository.AuthRepository;
import com.SecurityLockers.SecureDeliveryLockers.utility.EmailService;
import com.SecurityLockers.SecureDeliveryLockers.utility.JwtUtil;
import com.SecurityLockers.SecureDeliveryLockers.utility.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public User register(RegisterRequestDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Passwords don't match");
        }

         if (authRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        int otp = Util.generateUniqueOtp();
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .otp(otp)
                .isVerified(false)
                .build();

        authRepository.save(user);
           emailService.sendMail(dto.getEmail(), "Your OTP code: " + otp);
        return user;
    }

    @Transactional
    public String verifyOtp(String email, String otp) {
        Optional<User> userOptional = authRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();

        if (user.getIsVerified() != null && user.getIsVerified()) {
            throw new RuntimeException("User is already verified");
        }

        if (!String.valueOf(user.getOtp()).equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

         user.setIsVerified(true);
        authRepository.save(user);

        return jwtUtil.generateToken(user.getEmail());
    }
}
