package com.SecurityLockers.SecureDeliveryLockers.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskMessage implements Serializable {
    private TaskType taskType;
    private UUID reservationId;
    private Long userId;
    private Instant scheduledTime;
    private String email;
    private Object metadata;

    public enum TaskType {
        CHECK_EXPIRED_RESERVATIONS,
        SEND_REMINDER_NOTIFICATION,
        CLEANUP_EXPIRED_OTP,
        RELEASE_EXPIRED_LOCKERS
    }

    public static ScheduledTaskMessage createExpirationCheckTask(UUID reservationId, Instant scheduledTime) {
        return ScheduledTaskMessage.builder()
                .taskType(TaskType.CHECK_EXPIRED_RESERVATIONS)
                .reservationId(reservationId)
                .scheduledTime(scheduledTime)
                .build();
    }

    public static ScheduledTaskMessage createReminderTask(UUID reservationId, Long userId, String email, Instant scheduledTime) {
        return ScheduledTaskMessage.builder()
                .taskType(TaskType.SEND_REMINDER_NOTIFICATION)
                .reservationId(reservationId)
                .userId(userId)
                .email(email)
                .scheduledTime(scheduledTime)
                .build();
    }
}

