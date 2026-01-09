package com.SecurityLockers.SecureDeliveryLockers.utility;

import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthUtils {

    public User getCurrentUser() throws Exception {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new Exception("No authenticated user found");
    }
}
