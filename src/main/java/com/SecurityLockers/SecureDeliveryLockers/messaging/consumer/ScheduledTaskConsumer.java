package com.SecurityLockers.SecureDeliveryLockers.messaging.consumer;

import com.SecurityLockers.SecureDeliveryLockers.config.RabbitMQConfig;
import com.SecurityLockers.SecureDeliveryLockers.messaging.dto.EmailMessage;
import com.SecurityLockers.SecureDeliveryLockers.messaging.dto.ScheduledTaskMessage;
import com.SecurityLockers.SecureDeliveryLockers.messaging.producer.EmailProducer;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerReservation;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.LockerSlot;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerReservationRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerSlotRepository;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.model.User;
import com.SecurityLockers.SecureDeliveryLockers.modules.auth.repository.AuthRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class ScheduledTaskConsumer {

    @Autowired
    private LockerReservationRepository lockerReservationRepository;

    @Autowired
    private LockerSlotRepository lockerSlotRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private EmailProducer emailProducer;

    @RabbitListener(queues = RabbitMQConfig.SCHEDULED_TASK_QUEUE)
    public void consumeScheduledTask(ScheduledTaskMessage taskMessage) {
        try {
            log.info("Consuming scheduled task: {}", taskMessage.getTaskType());

            switch (taskMessage.getTaskType()) {
                case CHECK_EXPIRED_RESERVATIONS:
                    checkExpiredReservations();
                    break;

                case SEND_REMINDER_NOTIFICATION:
                    sendReminderNotification(taskMessage);
                    break;

                case CLEANUP_EXPIRED_OTP:
                    log.info("Cleanup expired OTP task - handled by Redis TTL");
                    break;

                case RELEASE_EXPIRED_LOCKERS:
                    releaseExpiredLockers();
                    break;

                default:
                    log.warn("Unknown task type: {}", taskMessage.getTaskType());
            }

            log.info("Scheduled task processed successfully: {}", taskMessage.getTaskType());
        } catch (Exception e) {
            log.error("Failed to process scheduled task {}: {}", 
                    taskMessage.getTaskType(), e.getMessage(), e);
            throw new RuntimeException("Failed to process scheduled task", e);
        }
    }

    private void checkExpiredReservations() {
        log.info("Checking for expired and expiring soon reservations...");
        Instant now = Instant.now();
        Instant oneHourFromNow = now.plusSeconds(3600); // 1 hour = 3600 seconds
        
        List<LockerReservation> allActiveReservations = lockerReservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == LockerReservation.ReservationStatus.ACTIVE)
                .filter(r -> r.getExpiresAt() != null)
                .toList();

        // Check for expired reservations (expiresAt < now)
        List<LockerReservation> expiredReservations = allActiveReservations.stream()
                .filter(r -> r.getExpiresAt().isBefore(now))
                .toList();

        // Check for reservations expiring soon (within 1 hour but not yet expired)
        List<LockerReservation> expiringSoonReservations = allActiveReservations.stream()
                .filter(r -> r.getExpiresAt().isAfter(now) && r.getExpiresAt().isBefore(oneHourFromNow))
                .filter(r -> r.getParcelPlacedAt() == null) // Only send reminder if parcel not yet placed
                .toList();

        // Process expired reservations
        for (LockerReservation reservation : expiredReservations) {
            try {
                reservation.setStatus(LockerReservation.ReservationStatus.EXPIRED);
                lockerReservationRepository.save(reservation);

                // DO NOT release the locker slot - keep it reserved/occupied
                // The slot should only be released when parcel is picked up or manually released
                // This ensures the locker remains locked and accessible for the user to renew OTP

                // Send expiration email
                User user = authRepository.findById(reservation.getUserId()).orElse(null);
                if (user != null) {
                    emailProducer.sendReservationExpiredEmail(user.getEmail());
                }

                log.info("Expired reservation processed (slot remains reserved): {}", reservation.getId());
            } catch (Exception e) {
                log.error("Failed to process expired reservation {}: {}", 
                        reservation.getId(), e.getMessage(), e);
            }
        }

        // Send reminders for reservations expiring soon
        for (LockerReservation reservation : expiringSoonReservations) {
            try {
                User user = authRepository.findById(reservation.getUserId()).orElse(null);
                if (user != null) {
                    // Only send reminder if we haven't sent one recently (could add a flag to track this)
                    emailProducer.sendReservationExpiringSoonEmail(user.getEmail());
                    log.info("Expiring soon reminder sent for reservation: {}", reservation.getId());
                }
            } catch (Exception e) {
                log.error("Failed to send expiring soon reminder for reservation {}: {}", 
                        reservation.getId(), e.getMessage(), e);
            }
        }
        
        log.info("Processed {} expired reservations and {} expiring soon reminders", 
                expiredReservations.size(), expiringSoonReservations.size());
    }

    private void sendReminderNotification(ScheduledTaskMessage taskMessage) {
        if (taskMessage.getReservationId() == null || taskMessage.getEmail() == null) {
            log.warn("Missing required fields for reminder notification");
            return;
        }

        Optional<LockerReservation> reservationOpt = 
                lockerReservationRepository.findById(taskMessage.getReservationId());

        if (reservationOpt.isPresent()) {
            LockerReservation reservation = reservationOpt.get();
            // Only send reminder if reservation is still active and not expired
            if (reservation.getStatus() == LockerReservation.ReservationStatus.ACTIVE) {
                Instant now = Instant.now();
                // Double-check it's actually expiring soon (within 1 hour)
                if (reservation.getExpiresAt() != null && 
                    reservation.getExpiresAt().isAfter(now) && 
                    reservation.getExpiresAt().isBefore(now.plusSeconds(3600))) {
                    
                    emailProducer.sendReservationExpiringSoonEmail(taskMessage.getEmail());
                    log.info("Expiring soon reminder sent for reservation: {}", taskMessage.getReservationId());
                } else {
                    log.debug("Skipping reminder for reservation {} - not expiring soon", taskMessage.getReservationId());
                }
            } else {
                log.debug("Skipping reminder for reservation {} - status is {}", 
                        taskMessage.getReservationId(), reservation.getStatus());
            }
        } else {
            log.warn("Reservation not found for reminder: {}", taskMessage.getReservationId());
        }
    }

    private void releaseExpiredLockers() {
        log.info("Releasing expired lockers...");
        checkExpiredReservations(); // This already handles releasing lockers
    }
}

