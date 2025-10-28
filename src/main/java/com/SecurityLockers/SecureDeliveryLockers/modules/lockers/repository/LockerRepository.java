package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.Locker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LockerRepository extends JpaRepository<Locker, UUID> {
}
