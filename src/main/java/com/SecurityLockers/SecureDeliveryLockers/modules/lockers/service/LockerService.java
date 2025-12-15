package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.service;

import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerSlotRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerReservationRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.Locker;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerReservation;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerReservationRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerSlotRepository;
import com.SecurityLockers.SecureDeliveryLockers.services.S3Service;
import com.SecurityLockers.SecureDeliveryLockers.utility.AuthUtils;
import com.SecurityLockers.SecureDeliveryLockers.utility.EmailService;
import com.SecurityLockers.SecureDeliveryLockers.utility.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private Executor asyncExecutor;
//    @Autowired
//    private LockerAsyncService lockerAsyncService;


//    public Locker createLocker(LockerRequestDTO lockerRequest) throws Exception {
//        CompletableFuture<String> imageUploadFuture;
//
//        if (lockerRequest.getLockerImage() != null && !lockerRequest.getLockerImage().isEmpty()) {
//            imageUploadFuture = CompletableFuture.supplyAsync(()-> {
//                try {
//               return s3Service.uploadFile(lockerRequest.getLockerImage());
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//
//        }else{
//            imageUploadFuture = CompletableFuture.completedFuture("");
//        }
//        String lockerImage = imageUploadFuture.get();
//        Locker locker = Locker.builder().
//                location(lockerRequest.getLocation()).
//                latitude(lockerRequest.getLatitude()).
//                lockerImage(lockerImage).
//                longitude(lockerRequest.getLongitude()).
//                totalSlots(lockerRequest.getTotalSlots()).
//                createdAt(Instant.now()).build();
//        return lockerRepository.save(locker);
//    }


    @Async("asyncExecutor")
    public CompletableFuture<Locker> createLockerAsync(LockerRequestDTO dto) {
        try {
            String imageUrl = null;

            if (dto.getLockerImage() != null && !dto.getLockerImage().isEmpty()) {
                imageUrl = s3Service.uploadFile(dto.getLockerImage());
            }

            Locker locker = Locker.builder()
                    .location(dto.getLocation())
                    .latitude(dto.getLatitude())
                    .longitude(dto.getLongitude())
                    .totalSlots(dto.getTotalSlots())
                    .lockerImage(imageUrl)
                    .createdAt(Instant.now())
                    .build();

            Locker savedLocker = lockerRepository.save(locker);

            return CompletableFuture.completedFuture(savedLocker);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
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
    public LockerReservation reserveLocker(LockerReservationRequestDTO dto) throws Exception {
        User user = authUtils.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User Not Found.");
        }
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
                .orElseThrow(() -> new RuntimeException("No suitable slots available for given parcel dimensions"));

        suitableSlot.setStatus(LockerSlot.Status.RESERVED);
        lockerSlotRepository.save(suitableSlot);

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(24 * 60 * 60);
        SecureRandom random = new SecureRandom();


        int myOtp = Util.generateUniqueOtp(random, lockerReservationRepository);
        int otherOtp = Util.generateUniqueOtp(random, lockerReservationRepository);

        LockerReservation reservation = LockerReservation.builder()
                .lockerSlot(suitableSlot)
                .userId(user.getId())
                .parcelHeight(dto.getHeight())
                .parcelWidth(dto.getWidth())
                .reservedAt(now)
                .expiresAt(expiry)
                .deliveryService(dto.getDeliveryService())
                .parcelDescription(dto.getParcelDescription())
                .userOtp(myOtp)
                .lockerState(LockerReservation.LockerState.LOCKED)
                .deliveryOtp(otherOtp)
                .status(LockerReservation.ReservationStatus.ACTIVE)
                .build();
        emailService.sendMail("omerkhan.dev1@gmail.com", String.valueOf(myOtp), String.valueOf(otherOtp));

        return lockerReservationRepository.save(reservation);
    }


    @Transactional
    public LockerReservation openLocker(Integer otp) {

        LockerReservation reservation = lockerReservationRepository.findActiveByOtp(otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Locker already accessed."));
        LockerSlot slot = reservation.getLockerSlot();
        Instant now = Instant.now();
        if (otp.equals(reservation.getDeliveryOtp())) {
            if (reservation.getParcelPlacedAt() != null) {
                throw new RuntimeException("This OTP has already been used to open the locker for delivery.");
            }
            reservation.setParcelPlacedAt(now);
            reservation.setLockerState(LockerReservation.LockerState.UNLOCKED);
            reservation.setStatus(LockerReservation.ReservationStatus.DELIVERED);
        } else if (otp.equals(reservation.getUserOtp())) {
            if (reservation.getParcelPickedAt() != null) {
                throw new RuntimeException("This OTP has already been used to open the locker for pickup.");
            }
            reservation.setParcelPickedAt(now);
            reservation.setLockerState(LockerReservation.LockerState.LOCKED);
            reservation.setStatus(LockerReservation.ReservationStatus.PICKED_UP);
            slot.setStatus(LockerSlot.Status.FREE);
            lockerSlotRepository.save(slot);
        }
        lockerReservationRepository.save(reservation);
        return reservation;

    }


}
