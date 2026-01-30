package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.service;

import com.SecurityLockers.SecureDeliveryLockers.messaging.producer.ScheduledTaskProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class ScheduledTaskService {

    @Autowired
    private ScheduledTaskProducer scheduledTaskProducer;

    @Scheduled(fixedRate = 3600000)
    public void scheduleExpirationChecks() {
        log.info("Scheduling expiration check tasks...");
        scheduledTaskProducer.sendScheduledTask(
                com.SecurityLockers.SecureDeliveryLockers.messaging.dto.ScheduledTaskMessage.builder()
                        .taskType(com.SecurityLockers.SecureDeliveryLockers.messaging.dto.ScheduledTaskMessage.TaskType.CHECK_EXPIRED_RESERVATIONS)
                        .scheduledTime(Instant.now())
                        .build()
        );
    }
}

