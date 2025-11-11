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
public class LockerReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_slot_id", nullable = false)
    private LockerSlot lockerSlot;


    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "delivery_service",nullable = false)
    private String deliveryService;



    @Column(name = "parcel_height", nullable = false)
    private double parcelHeight;

    @Column(name = "parcel_width", nullable = false)
    private double parcelWidth;

    @Column(name = "parcel_description")
    private String parcelDescription;

     @Column(name = "user_otp", nullable = false)
    private Integer userOtp;

    @Column(name = "delivery_otp", nullable = false)
    private Integer deliveryOtp;


    @Column(name = "reserved_at", nullable = false)
    private Instant reservedAt = Instant.now();

    @Column(name = "parcel_placed_at")
    private Instant parcelPlacedAt;

    @Column(name = "parcel_picked_at")
    private Instant parcelPickedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "locker_state", nullable = false)
    private LockerState lockerState = LockerState.LOCKED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    public enum ReservationStatus {
        ACTIVE,
        DELIVERED,
        PICKED_UP,
        CANCELLED,
        EXPIRED
    }

    public enum LockerState {
        LOCKED,
        UNLOCKED
    }
}
