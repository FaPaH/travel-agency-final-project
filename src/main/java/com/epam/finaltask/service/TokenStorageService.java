package com.epam.finaltask.service;

import java.util.UUID;

public interface TokenStorageService {

    String storeRefreshToken(String id, String refreshToken);

    String getRefreshToken(String id);

    void revokeRefreshToken(String id);
}
