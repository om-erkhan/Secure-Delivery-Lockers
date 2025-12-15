package com.SecurityLockers.SecureDeliveryLockers.modules.auth.service;

import com.SecurityLockers.SecureDeliveryLockers.modules.auth.dto.RegisterRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.repository.AuthRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.AuthResponse;
import com.SecurityLockers.SecureDeliveryLockers.utility.AuthEnums;
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

    public AuthResponse login(RegisterRequestDTO dto) {
        Optional<User> optionalUser = authRepository.findByEmail(dto.getEmail());
//       for user login
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return new AuthResponse(null, "Wrong Password", AuthEnums.WRONG_PASSWORD);
            }
            if (!user.getIsVerified()) {
                return new AuthResponse(null, "User exists but not verified. Please verify OTP.", AuthEnums.OTP_REQUIRED);
            }
            String token = jwtUtil.generateToken(user.getEmail());
            return new AuthResponse(token, "Login successful", AuthEnums.LOGIN_SUCCESS);
        }
//        for signup
        int otp = Util.generateUniqueOtp();
        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        User newUser = User.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .otp(otp)
                .isVerified(false)
                .build();
        authRepository.save(newUser);
        emailService.sendMail(dto.getEmail(), "Your OTP code: " + otp);
        return new AuthResponse(null, "Otp sent to your email. Please verify.", AuthEnums.OTP_SENT);
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
