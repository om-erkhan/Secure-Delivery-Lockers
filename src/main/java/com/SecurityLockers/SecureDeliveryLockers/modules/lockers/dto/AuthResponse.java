package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto;

import com.SecurityLockers.SecureDeliveryLockers.utility.AuthEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String message;
    private AuthEnums status;
}

