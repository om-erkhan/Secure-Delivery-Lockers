package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository;
import java.util.List;
import java.util.UUID;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LockerSlotRepository extends JpaRepository<LockerSlot, UUID> {
    List<LockerSlot> findByLockerIdAndStatus(UUID lockerId, LockerSlot.Status status);

//    @Query("SELECT COALESCE(MAX(ls.slotNumber), 0) FROM LockerSlot ls WHERE ls.locker.id = :lockerId")
//    Integer findMaxSlotNumberByLockerId(UUID lockerId);

}
