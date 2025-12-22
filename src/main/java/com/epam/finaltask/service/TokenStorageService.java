package com.epam.finaltask.service;

import java.util.UUID;

public interface TokenStorageService {

    String storeRefreshToken(UUID id, String refreshToken);

    String getRefreshToken(UUID id);

    void revokeRefreshToken(UUID id);
}
