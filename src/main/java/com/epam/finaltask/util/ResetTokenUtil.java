package com.epam.finaltask.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ResetTokenUtil {

    private final static SecureRandom secureRandom = new SecureRandom();
    private final static Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public String generateResetToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
