package com.epam.finaltask.model;


import lombok.Getter;

import java.time.Duration;

@Getter
public enum CacheType {

    REFRESH_TOKENS(CacheNames.REFRESH_TOKENS, Duration.ZERO, 1000),
    RESET_TOKENS(CacheNames.RESET_TOKENS, Duration.ZERO, 100),
    VOUCHER_PAGES(CacheNames.VOUCHER_PAGES, Duration.ZERO, 100),
    USER_PROFILES(CacheNames.USER_PROFILES, Duration.ZERO, 500);

    public static class CacheNames {
        public static final String REFRESH_TOKENS = "refreshTokens";
        public static final String RESET_TOKENS = "resetTokens";
        public static final String VOUCHER_PAGES = "voucherPages";
        public static final String USER_PROFILES = "userProfiles";
    }

    private final String cacheName;
    private final Duration ttl;
    private final long maxSize;

    CacheType(String cacheName, Duration ttl, long maxSize) {
        this.cacheName = cacheName;
        this.ttl = ttl;
        this.maxSize = maxSize;
    }

}
