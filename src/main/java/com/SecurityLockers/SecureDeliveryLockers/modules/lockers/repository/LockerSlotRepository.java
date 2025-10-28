package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository;
import java.util.List;
import java.util.UUID;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LockerSlotRepository extends JpaRepository<LockerSlot, UUID> {
    List<LockerSlot> findByLockerIdAndStatus(UUID lockerId, LockerSlot.Status status);

}
