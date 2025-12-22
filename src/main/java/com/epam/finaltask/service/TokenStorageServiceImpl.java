package com.epam.finaltask.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.epam.finaltask.model.CacheType.CacheNames.REFRESH_TOKENS;

@Service
@RequiredArgsConstructor
public class TokenStorageServiceImpl implements TokenStorageService {

    @Override
    @CachePut(value = REFRESH_TOKENS, key = "#userId")
    public String storeRefreshToken(UUID userId, String refreshToken) {
        return refreshToken;
    }

    @Override
    @Cacheable(value = REFRESH_TOKENS, key = "#userId", unless = "#result == null")
    public String getRefreshToken(UUID userId) {
        return null;
    }

    @Override
    @CacheEvict(value = REFRESH_TOKENS, key = "#userId")
    public void revokeRefreshToken(UUID userId) {

    }
}
