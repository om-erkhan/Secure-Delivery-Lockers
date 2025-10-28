package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockerSlotRequestDTO {
    private Integer slotNumber;
    private LockerSlot.Size size;
    private LockerSlot.Status status;
}
