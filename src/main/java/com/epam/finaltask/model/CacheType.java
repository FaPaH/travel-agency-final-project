package com.epam.finaltask.model;


import lombok.Getter;

import java.time.Duration;

@Getter
public enum CacheType {

    REFRESH_TOKENS(CacheNames.REFRESH_TOKENS, Duration.ZERO, 10000);

    public static class CacheNames {
        public static final String REFRESH_TOKENS = "refreshTokens";
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
