package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "locker_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LockerSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id", nullable = false)
    @JsonBackReference
    private Locker locker;

    @Column(name = "slot_number")
    private Integer slotNumber;

    @Enumerated(EnumType.STRING)
    private Size size;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum Size {
        SM, MD, LG
    }

    public enum Status {
        FREE, OCCUPIED, RESERVED
    }
}

