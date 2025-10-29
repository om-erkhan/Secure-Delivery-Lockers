package com.SecurityLockers.SecureDeliveryLockers.modules.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    private String email;
    private String password;
    private String confirmPassword;




}
