package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "lockers")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String location;
    private Double latitude;
    private Double longitude;
    private String lockerImage;

    @Column(name = "total_slots")
    private Integer totalSlots;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "locker", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LockerSlot> lockerSlots;



}
