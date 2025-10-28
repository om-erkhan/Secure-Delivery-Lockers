package com.SecurityLockers.SecureDeliveryLockers.utility;

import com.SecurityLockers.SecureDeliveryLockers.modules.lockers.repository.LockerReservationRepository;

import java.security.SecureRandom;

public class Util {
    public static int generateUniqueOtp(SecureRandom random, LockerReservationRepository lockerReservationRepository) {
        int otp;
        boolean exists;
        do {
            otp = 1000 + random.nextInt(9000);
            exists = lockerReservationRepository.existsByMyOtpOrOtherOtp(otp, otp);
        } while (exists);
        return otp;
    }
}
