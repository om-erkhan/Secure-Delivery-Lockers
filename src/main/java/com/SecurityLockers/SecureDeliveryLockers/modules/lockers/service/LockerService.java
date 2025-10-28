package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.service;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerSlotRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerReservationRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.Locker;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerReservation;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerReservationRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerSlotRepository;
import com.SecurityLockers.SecureDeliveryLockers.utility.EmailService;
import com.SecurityLockers.SecureDeliveryLockers.utility.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class LockerService {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private LockerReservationRepository lockerReservationRepository;

    @Autowired
    private LockerSlotRepository lockerSlotRepository;

    @Autowired
    private EmailService emailService;


    public Locker createLocker(LockerRequestDTO lockerRequest) {
        Locker locker = new Locker();
        locker.setLocation(lockerRequest.getLocation());
        locker.setLatitude(lockerRequest.getLatitude());
        locker.setLongitude(lockerRequest.getLongitude());
        locker.setTotalSlots(lockerRequest.getTotalSlots());
        locker.setCreatedAt(Instant.now());
        return lockerRepository.save(locker);
    }

    public List<Locker> getLockers() {
        return lockerRepository.findAll();
    }

    public LockerSlot createLockerSlot(UUID lockerID, LockerSlotRequestDTO dto) {
        Locker locker = lockerRepository.findById(lockerID)
                .orElseThrow(() -> new RuntimeException("Locker Not Found with ID: " + lockerID));
        LockerSlot slot = LockerSlot.builder()
                .locker(locker)
                .slotNumber(dto.getSlotNumber())
                .size(dto.getSize())
                .status(dto.getStatus())
                .createdAt(Instant.now())
                .build();

        return lockerSlotRepository.save(slot);
    }

    @Transactional
    public LockerReservation reserveLocker(LockerReservationRequestDTO dto) {
         Locker locker = lockerRepository.findById(dto.getLockerId())
                .orElseThrow(() -> new RuntimeException("Locker not found with ID: " + dto.getLockerId()));

         List<LockerSlot> freeSlots = lockerSlotRepository.findByLockerIdAndStatus(dto.getLockerId(), LockerSlot.Status.FREE);

        if (freeSlots.isEmpty()) {
            throw new RuntimeException("No Free Slots Available at this time.");
        }

         LockerSlot suitableSlot = freeSlots.stream()
                .filter(slot -> {
                    return switch (slot.getSize()) {
                        case SM -> dto.getHeight() <= 10 && dto.getWidth() <= 10;
                        case MD -> dto.getHeight() <= 20 && dto.getWidth() <= 20;
                        case LG -> dto.getHeight() <= 40 && dto.getWidth() <= 40;
                        default -> false;
                    };
                })
                .min(Comparator.comparing(slot -> slot.getSize().ordinal()))
                .orElseThrow(() -> new RuntimeException("No suitable slot available for given parcel dimensions"));

         suitableSlot.setStatus(LockerSlot.Status.RESERVED);
        lockerSlotRepository.save(suitableSlot);

         UUID hardcodedUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

         Instant now = Instant.now();
        Instant expiry = now.plusSeconds(24 * 60 * 60);
        SecureRandom random = new SecureRandom();


        int myOtp = Util.generateUniqueOtp(random, lockerReservationRepository);
        int otherOtp = Util.generateUniqueOtp(random, lockerReservationRepository);

         LockerReservation reservation = LockerReservation.builder()
                .lockerSlot(suitableSlot)
                .userId(hardcodedUserId)
                .parcelHeight(dto.getHeight())
                .parcelWidth(dto.getWidth())
                .reservedAt(now)
                .expiresAt(expiry)
                 .myOtp(myOtp)
                 .otherOtp(otherOtp)
                 .status(LockerReservation.ReservationStatus.ACTIVE)
                 .build();
         emailService.sendMail("omerkhan.dev1@gmail.com",String.valueOf(myOtp) ,String.valueOf(otherOtp)  );

        return lockerReservationRepository.save(reservation);
    }

}
