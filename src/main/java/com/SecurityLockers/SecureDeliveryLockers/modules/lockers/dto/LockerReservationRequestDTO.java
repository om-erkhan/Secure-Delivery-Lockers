package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
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

    private LockerSlot.Size size;
    private String parcelValue;
    private String parcelDescription;
    private String deliveryService;
    private String specialInstructions;
    private double contactNumber;
}
