package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockerRequestDTO {
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer totalSlots;
}
