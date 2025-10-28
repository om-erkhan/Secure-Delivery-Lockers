package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LockerReservationRepository  extends JpaRepository<LockerReservation, UUID> {
    boolean existsByUserOtpOrDeliveryOtp(Integer userOtp, Integer deliveryOtp);
    Optional<LockerReservation> findByUserOtpOrDeliveryOtp(Integer userOtp, Integer deliveryOtp);

}
