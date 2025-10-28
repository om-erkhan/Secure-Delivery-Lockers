package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LockerReservationRepository  extends JpaRepository<LockerReservation, UUID> {
    boolean existsByUserOtpOrDeliveryOtp(Integer userOtp, Integer deliveryOtp);
    @Query("SELECT r FROM LockerReservation r WHERE (r.userOtp = :otp OR r.deliveryOtp = :otp) AND r.status NOT IN ('DELIVERED', 'PICKED_UP')")
    Optional<LockerReservation> findActiveByOtp(@Param("otp") Integer otp);

}
