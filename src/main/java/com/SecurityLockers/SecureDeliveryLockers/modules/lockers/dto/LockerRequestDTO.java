package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockerRequestDTO {
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer totalSlots;
    private MultipartFile lockerImage;
}
