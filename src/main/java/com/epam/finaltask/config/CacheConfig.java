package com.epam.finaltask.config;

import com.epam.finaltask.model.CacheType;
import com.epam.finaltask.util.JwtProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        List<CaffeineCache> caches = Arrays.stream(CacheType.values())
                .map(this::buildCache)
                .collect(Collectors.toList());

        cacheManager.setCaches(caches);
        return cacheManager;
    }

    private CaffeineCache buildCache(CacheType type) {
        return new CaffeineCache(
                type.getCacheName(),
                Caffeine.newBuilder()
                        .expireAfterWrite(resolveTtl(type))
                        .maximumSize(type.getMaxSize())
                        .recordStats()
                        .build()
        );
    }

    private Duration resolveTtl(CacheType type) {

        if (type == CacheType.REFRESH_TOKENS) {
            return Duration.ofMillis(jwtProperties.getRefreshToken().getExpiration());
        }

        return type.getTtl();
    }
}
