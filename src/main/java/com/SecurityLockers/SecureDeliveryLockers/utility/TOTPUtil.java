package com.SecurityLockers.SecureDeliveryLockers.utility;
import de.taimos.totp.TOTP;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;

public class TOTPUtil {

     public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

     public static boolean verifyCode(String secret, String code) {
         for (int i = -1; i <= 1; i++) {
            String candidate = TOTP.getOTP(secret);
            if (candidate.equals(code)) return true;
        }
        return false;
    }
}
