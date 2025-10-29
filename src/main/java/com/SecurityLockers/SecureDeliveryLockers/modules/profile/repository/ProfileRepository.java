package com.SecurityLockers.SecureDeliveryLockers.modules.profile.repository;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileRepository  extends JpaRepository<UserProfile, UUID> {
}
