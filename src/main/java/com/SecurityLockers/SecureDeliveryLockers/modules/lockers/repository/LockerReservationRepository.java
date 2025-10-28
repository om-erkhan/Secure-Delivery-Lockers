package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LockerReservationRepository  extends JpaRepository<LockerReservation, UUID> {
    boolean existsByMyOtpOrOtherOtp(double myOtp, double otherOtp);

}
