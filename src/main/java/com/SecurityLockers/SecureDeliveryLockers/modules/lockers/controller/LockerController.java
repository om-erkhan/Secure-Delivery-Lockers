package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.controller;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerReservationRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerSlotRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.Locker;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerReservation;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.service.LockerService;
import com.SecurityLockers.SecureDeliveryLockers.utility.ResponseBuilder;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lockers")
public class LockerController {


    @Autowired
    private LockerService lockerService;

    @PostMapping("/create")
    public ResponseEntity<?> createLocker(@RequestBody LockerRequestDTO lockerRequestDTO) {
        Locker createdLocker = lockerService.createLocker(lockerRequestDTO);
        return ResponseBuilder.success(createdLocker, "Locker created successfully");
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllLockers() {
        List<Locker> allLockers = lockerService.getLockers();
        return ResponseBuilder.success(allLockers, "All Lockers Fetched Successfully");
    }


    @PostMapping("/{lockerId}/create-slot")
    public ResponseEntity<?> createLockerSlot(@PathVariable("lockerId") UUID lockerId, @RequestBody LockerSlotRequestDTO dto) {
        LockerSlot slot = lockerService.createLockerSlot(lockerId, dto);
        return ResponseBuilder.success(slot, "Slot Created Successfully");
    }

    @PostMapping("/reserve-locker")
    public ResponseEntity<?> reserveLocker(@RequestBody LockerReservationRequestDTO dto) {
        return ResponseBuilder.success(lockerService.reserveLocker(dto), "Reserved Successfully");
    }


    @PostMapping("/open-locker")
    public ResponseEntity<?> openLocker(@RequestBody Map<String, Object> payload){
        Integer otp = (Integer) payload.get("otp");
        LockerReservation locker = lockerService.openLocker(otp);
        return ResponseBuilder.success(locker, "Opened Successfully");
    }
}
