package com.epam.finaltask.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "application.security.jwt")
@Component
public class JwtProperties {

    private String secretKey;

    private long expiration;

    private final RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class RefreshToken {

        private long expiration;
    }
}
