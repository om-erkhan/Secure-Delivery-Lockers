package com.SecurityLockers.SecureDeliveryLockers.modules.lockers.service;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.dto.LockerRequestDTO;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.model.Locker;
import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerRepository;
import com.SecurityLockers.SecureDeliveryLockers.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LockerAsyncService {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private LockerRepository lockerRepository;


}
