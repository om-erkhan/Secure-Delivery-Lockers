package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.controller;

import com.SecurityLockers.SecureDeliveryLockers.entity.ApiResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/lockers")
public class LockerController {


    @Autowired
    private LockerService lockerService;

    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createLocker(
            @ModelAttribute LockerRequestDTO lockerRequestDTO) {

        try {
            Locker res = lockerService.createLocker(lockerRequestDTO);
            return ResponseBuilder.success(
                    res,
                    "Locker Created Successfully"
            );
        }catch (Exception ex){
            return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

        }

    }


    @GetMapping("/get-all")
    public ResponseEntity<?> getAllLockers() {
        List<Locker> allLockers = lockerService.getLockers();
        return ResponseBuilder.success(allLockers, "All Lockers Fetched Successfully");
    }


    @PostMapping("/create-slot/{lockerId}")
    public ResponseEntity<?> createLockerSlot(@PathVariable("lockerId") UUID lockerId, @RequestBody LockerSlotRequestDTO dto) {
        LockerSlot slot = lockerService.createLockerSlot(lockerId, dto);
        return ResponseBuilder.success(slot, "Slot Created Successfully");
    }

    @PostMapping("/reserve-locker/{lockerId}")
    public ResponseEntity<?> reserveLocker( @PathVariable("lockerId") UUID lockerId, @RequestBody LockerReservationRequestDTO dto) throws Exception {
        return ResponseBuilder.success(lockerService.reserveLocker(lockerId,dto), "Reserved Successfully");
    }


    @PostMapping("/open-locker")
    public ResponseEntity<?> openLocker(@RequestBody Map<String, Object> payload) {
        Integer otp = (Integer) payload.get("otp");
        LockerReservation locker = lockerService.openLocker(otp);
        return ResponseBuilder.success(locker, "Opened Successfully");
    }

    @GetMapping("/get-reservations")
    public ResponseEntity<?> getReservations() {
        try {
            List<LockerReservation> reservations = lockerService.getReservations();
            return ResponseBuilder.success(reservations, "Your Reservations Fetched Successfully");
        } catch (Exception e) {
            return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
