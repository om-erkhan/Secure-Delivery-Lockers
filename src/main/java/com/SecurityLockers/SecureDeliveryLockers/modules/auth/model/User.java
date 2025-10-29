package com.SecurityLockers.SecureDeliveryLockers.modules.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private Boolean isVerified = false;

    @Column(unique = true, nullable = false)
    @JsonIgnore
    private int otp;


    private Instant createdAt = Instant.now();
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("user")
    private UserProfile userProfile;


}
