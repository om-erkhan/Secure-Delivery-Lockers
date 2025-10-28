package com.SecurityLockers.SecureDeliveryLockers.modules.auth.repository;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuthRepository extends JpaRepository<UserModel, UUID> {
}
