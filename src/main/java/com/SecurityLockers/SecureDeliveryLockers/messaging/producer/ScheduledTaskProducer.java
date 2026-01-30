package com.SecurityLockers.SecureDeliveryLockers.messaging.producer;

import com.SecurityLockers.SecureDeliveryLockers.config.RabbitMQConfig;
import com.SecurityLockers.SecureDeliveryLockers.messaging.dto.ScheduledTaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class ScheduledTaskProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendScheduledTask(ScheduledTaskMessage taskMessage) {
        try {
            log.info("Sending scheduled task to queue: {}", taskMessage.getTaskType());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SCHEDULED_TASK_EXCHANGE,
                    RabbitMQConfig.SCHEDULED_TASK_ROUTING_KEY,
                    taskMessage
            );
            log.info("Scheduled task sent successfully to queue: {}", taskMessage.getTaskType());
        } catch (Exception e) {
            log.error("Failed to send scheduled task to queue: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to queue scheduled task", e);
        }
    }

    public void scheduleExpirationCheck(UUID reservationId, Instant expirationTime) {
        ScheduledTaskMessage taskMessage = ScheduledTaskMessage.createExpirationCheckTask(
                reservationId,
                expirationTime
        );
        sendScheduledTask(taskMessage);
    }

    public void scheduleReminderNotification(UUID reservationId, Long userId, String email, Instant reminderTime) {
        ScheduledTaskMessage taskMessage = ScheduledTaskMessage.createReminderTask(
                reservationId,
                userId,
                email,
                reminderTime
        );
        sendScheduledTask(taskMessage);
    }
}

