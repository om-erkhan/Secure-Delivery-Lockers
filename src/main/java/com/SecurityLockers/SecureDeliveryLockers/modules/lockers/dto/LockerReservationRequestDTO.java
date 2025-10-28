package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockerReservationRequestDTO {
    private Integer height;
    private Integer width;
    private String parcelDescription;
    private String deliveryService;
    private UUID lockerId;
}
