package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.service;

import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.repository.AuthRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;


@Slf4j
@Component
public class LockerService {

    @Autowired
    private AuthRepository authRepository;

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


    @Autowired
    private StringRedisTemplate redisTemplate;


    public Locker createLocker(LockerRequestDTO dto) throws Exception {
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
        return lockerRepository.save(locker);
    }


    public List<Locker> getLockers() {
        return lockerRepository.findAll();
    }

    @Transactional
    public LockerSlot createLockerSlot(UUID lockerID, LockerSlotRequestDTO dto) {
        Locker locker = lockerRepository.findById(lockerID)
                .orElseThrow(() -> new RuntimeException("Locker Not Found with ID: " + lockerID));
        log.info("Got locker data:  {}",
                locker);
        log.info("Got locker slots No:  {}",
                locker.getLockerSlots().size());
        LockerSlot slot = LockerSlot.builder()
                .locker(locker)
                .slotNumber(locker.getLockerSlots().size() + 1)
                .size(dto.getSize())
                .status(dto.getStatus())
                .createdAt(Instant.now())
                .build();
        locker.setTotalSlots(locker.getLockerSlots().size() + 1);
        lockerRepository.save(locker);

        return lockerSlotRepository.save(slot);
    }

    @Transactional
    public LockerReservation reserveLocker( UUID lockerId, LockerReservationRequestDTO dto) throws Exception {
        User user = authUtils.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User Not Found.");
        }
        Locker locker = lockerRepository.findById(lockerId)
                .orElseThrow(() -> new RuntimeException("Locker not found with ID: " + lockerId));

        List<LockerSlot> freeSlots = lockerSlotRepository.findByLockerIdAndStatus(lockerId, LockerSlot.Status.FREE);

        if (freeSlots.isEmpty()) {
            throw new RuntimeException("No Free Slots Available at this time.");
        }

        LockerSlot suitableSlot = freeSlots.stream()
                .filter(slot -> slot.getSize() == dto.getSize())
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No suitable slots available for size: " + dto.getSize())
                );


        suitableSlot.setStatus(LockerSlot.Status.RESERVED);
        lockerSlotRepository.save(suitableSlot);

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(24 * 60 * 60);
        SecureRandom random = new SecureRandom();

        int myOtp = Util.generateUniqueOtp(random, lockerReservationRepository);
        int otherOtp = Util.generateUniqueOtp(random, lockerReservationRepository);

        String userOtpKey = "OTP:USER:" + user.getId() + ":" + lockerId;
        String deliveryOtpKey = "OTP:DELIVERY:" + user.getId() + ":" + lockerId;

        redisTemplate.opsForValue().set(userOtpKey, String.valueOf(myOtp), Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(deliveryOtpKey, String.valueOf(otherOtp), Duration.ofMinutes(30));


        LockerReservation reservation = LockerReservation.builder()
                .lockerSlot(suitableSlot)
                .userId(user.getId())
                 .reservedAt(now)
                .expiresAt(expiry)
                .parcelValue(dto.getParcelValue())
                .parcelDescription(dto.getParcelDescription())
                .specialInstructions(dto.getSpecialInstructions())
                .contactNumber(dto.getContactNumber())
                .deliveryService(dto.getDeliveryService())
                .userOtp(myOtp)
                .lockerState(LockerReservation.LockerState.LOCKED)
                .deliveryOtp(otherOtp)
                .status(LockerReservation.ReservationStatus.ACTIVE)
                .build();
//        have to change the mailing address to the request user email
        emailService.sendMail("omerkhan.dev1@gmail.com", String.valueOf(myOtp), String.valueOf(otherOtp));

        return lockerReservationRepository.save(reservation);
    }


    @Transactional
    public LockerReservation openLocker(Integer otp) {

        LockerReservation reservation = null;

        List<LockerReservation> activeReservations = lockerReservationRepository.findAll(); // keep original DB logic

        for (LockerReservation r : activeReservations) {
            String userKey = "OTP:USER:" + r.getUserId() + ":" + r.getId();
            String deliveryKey = "OTP:DELIVERY:" + r.getUserId() + ":" + r.getId();

            String cachedUserOtp = redisTemplate.opsForValue().get(userKey);
            String cachedDeliveryOtp = redisTemplate.opsForValue().get(deliveryKey);

            if (cachedUserOtp != null && otp.equals(Integer.parseInt(cachedUserOtp))) {
                reservation = r;
                redisTemplate.delete(userKey); // remove from cache
                break;
            } else if (cachedDeliveryOtp != null && otp.equals(Integer.parseInt(cachedDeliveryOtp))) {
                reservation = r;
                redisTemplate.delete(deliveryKey);
                break;
            }
        }

         if (reservation == null) {
            reservation = lockerReservationRepository.findByAnyOtp(otp)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));
        }


          reservation = lockerReservationRepository.findByAnyOtp(otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Locker already accessed."));
        LockerSlot slot = reservation.getLockerSlot();
        User user = authRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for this reservation."));

        Instant now = Instant.now();
        if (otp.equals(reservation.getDeliveryOtp())) {
            if (reservation.getParcelPlacedAt() != null) {
                throw new RuntimeException("This OTP has already been used to open the locker for delivery.");
            }
            reservation.setParcelPlacedAt(now);
            reservation.setLockerState(LockerReservation.LockerState.UNLOCKED);
            reservation.setStatus(LockerReservation.ReservationStatus.DELIVERED);
            emailService.sendMail(user.getEmail(), "Locker Update", "Your Parcel has been placed successfully","");
        } else if(otp.equals(reservation.getUserOtp()) && reservation.getParcelPlacedAt() == null){
            throw new RuntimeException("The Parcel is not being delivered till now, please try again after parcel is delivered.");
        }else if (otp.equals(reservation.getUserOtp())) {
            if (reservation.getParcelPickedAt() != null) {
                throw new RuntimeException("This OTP has already been used to open the locker for pickup.");
            }
            reservation.setParcelPickedAt(now);
            reservation.setLockerState(LockerReservation.LockerState.LOCKED);
            reservation.setStatus(LockerReservation.ReservationStatus.PICKED_UP);
            slot.setStatus(LockerSlot.Status.FREE);
            lockerSlotRepository.save(slot);
            emailService.sendMail(user.getEmail(), "Locker Update", "Your Parcel has been picked successfully, don't forget to rate us :P","");

        }
        lockerReservationRepository.save(reservation);
        return reservation;

    }
    public List<LockerReservation> getReservations() {
        return lockerReservationRepository.findAll();
    }



}
