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
import com.SecurityLockers.SecureDeliveryLockers.messaging.producer.EmailProducer;
import com.SecurityLockers.SecureDeliveryLockers.messaging.producer.FileUploadProducer;
import com.SecurityLockers.SecureDeliveryLockers.messaging.producer.ScheduledTaskProducer;
import com.SecurityLockers.SecureDeliveryLockers.services.S3Service;
import com.SecurityLockers.SecureDeliveryLockers.utility.AuthUtils;
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
    private EmailProducer emailProducer;

    @Autowired
    private FileUploadProducer fileUploadProducer;

    @Autowired
    private ScheduledTaskProducer scheduledTaskProducer;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private Executor asyncExecutor;


    public Locker createLocker(LockerRequestDTO dto) throws Exception {
        // Create locker first without image
        Locker locker = Locker.builder()
                .location(dto.getLocation())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .totalSlots(dto.getTotalSlots())
                .lockerImage(null) // Will be updated after async upload
                .createdAt(Instant.now())
                .build();
        Locker savedLocker = lockerRepository.save(locker);

        // Queue file upload if image provided
        if (dto.getLockerImage() != null && !dto.getLockerImage().isEmpty()) {
            try {
                fileUploadProducer.queueLockerImageUpload(dto.getLockerImage(), savedLocker.getId());
                log.info("Locker image upload queued for locker: {}", savedLocker.getId());
            } catch (Exception e) {
                log.error("Failed to queue locker image upload: {}", e.getMessage(), e);
                // Continue without image - can be uploaded later
            }
        }

        return savedLocker;
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
    public LockerReservation reserveLocker(UUID lockerId, LockerReservationRequestDTO dto) throws Exception {
        User user = authUtils.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User Not Found.");
        }
        lockerRepository.findById(lockerId)
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
        // Save reservation first
        LockerReservation savedReservation = lockerReservationRepository.save(reservation);
        
        // Queue email sending (using user's email instead of hardcoded)
        emailProducer.sendReservationOtpEmail(user.getEmail(), String.valueOf(myOtp), String.valueOf(otherOtp));
        
        // Schedule reminder email 1 hour before expiration
        Instant reminderTime = expiry.minusSeconds(3600); // 1 hour before expiration
        if (reminderTime.isAfter(now)) {
            scheduledTaskProducer.scheduleReminderNotification(
                    savedReservation.getId(),
                    user.getId(),
                    user.getEmail(),
                    reminderTime
            );
            log.info("Scheduled reminder notification for reservation {} at {}", 
                    savedReservation.getId(), reminderTime);
        }

        return savedReservation;
    }


    @Transactional
    public LockerReservation openLocker(Integer otp) {


        List<LockerReservation> activeReservations = lockerReservationRepository.findAll();


        LockerReservation reservation = lockerReservationRepository.findByAnyOtp(otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP. Locker already accessed."));
        LockerSlot slot = reservation.getLockerSlot();
        User user = authRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for this reservation."));

        Instant now = Instant.now();
        
        // Check if reservation has expired
        if (reservation.getStatus() == LockerReservation.ReservationStatus.EXPIRED || 
            (reservation.getExpiresAt() != null && reservation.getExpiresAt().isBefore(now))) {
            
            // Mark as expired if not already marked
            if (reservation.getStatus() != LockerReservation.ReservationStatus.EXPIRED) {
                reservation.setStatus(LockerReservation.ReservationStatus.EXPIRED);
                lockerReservationRepository.save(reservation);
            }
            
            // Send email telling user to renew OTP
            emailProducer.sendRenewOtpRequiredEmail(user.getEmail());
            
            throw new RuntimeException("Your reservation has expired. Please renew your OTP from the app to access the locker.");
        }
        if (otp.equals(reservation.getDeliveryOtp())) {
            if (reservation.getParcelPlacedAt() != null) {
                throw new RuntimeException("This OTP has already been used to open the locker for delivery.");
            }
            reservation.setParcelPlacedAt(now);
            reservation.setLockerState(LockerReservation.LockerState.UNLOCKED);
            reservation.setStatus(LockerReservation.ReservationStatus.DELIVERED);
            emailProducer.sendParcelDeliveredEmail(user.getEmail());
        } else if (otp.equals(reservation.getUserOtp()) && reservation.getParcelPlacedAt() == null) {
            throw new RuntimeException("The Parcel is not being delivered till now, please try again after parcel is delivered.");
        } else if (otp.equals(reservation.getUserOtp())) {
            if (reservation.getParcelPickedAt() != null) {
                throw new RuntimeException("This OTP has already been used to open the locker for pickup.");
            }
            reservation.setParcelPickedAt(now);
            reservation.setLockerState(LockerReservation.LockerState.LOCKED);
            reservation.setStatus(LockerReservation.ReservationStatus.PICKED_UP);
            slot.setStatus(LockerSlot.Status.FREE);
            lockerSlotRepository.save(slot);
            emailProducer.sendParcelPickedUpEmail(user.getEmail());

        }
        lockerReservationRepository.save(reservation);
        return reservation;

    }

    public List<LockerReservation> getReservations() throws Exception {
         User currentUser = authUtils.getCurrentUser();
         return lockerReservationRepository.findByUserId(currentUser.getId());
    }



}
