package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "locker_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class    LockerReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

     @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_slot_id", nullable = false)
    private LockerSlot lockerSlot;

     @Column(name = "user_id")
    private UUID userId;

     @Column(name = "parcel_height", nullable = false)
    private double parcelHeight;

    @Column(name = "parcel_width", nullable = false)
    private double parcelWidth;

     @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt = Instant.now();

     @Column(name="my_otp" , nullable = false)
     private double myOtp;

    @Column(name="other_otp" , nullable = false)
    private double otherOtp;

    @Column(name = "expires_at")
    private Instant expiresAt;

     @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    public enum ReservationStatus {
        ACTIVE, COMPLETED, CANCELLED, EXPIRED
    }
}
