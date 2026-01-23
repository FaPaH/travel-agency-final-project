package com.epam.finaltask.service.impl;

import com.epam.finaltask.service.AttemptService;
import com.epam.finaltask.service.TokenStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttemptServiceImpl implements AttemptService {

    private final TokenStorageService<Integer> failedAttemptStorage;
    private final int limit = 5;

    @Override
    public void track(String ip) {
        String key = "login:fail:" + ip;
        int count = failedAttemptStorage.get(key) == null ? 0 : failedAttemptStorage.get(key);
        failedAttemptStorage.store(key, count + 1);
    }

    @Override
    public boolean isBlocked(String ip) {
        String key = "login:fail:" + ip;
        int value = failedAttemptStorage.get(key) == null ? 0 : failedAttemptStorage.get(key);
        return value >= limit;
    }

    @Override
    public void clearBlocked(String ip) {
        failedAttemptStorage.revoke(ip);
    }
}
