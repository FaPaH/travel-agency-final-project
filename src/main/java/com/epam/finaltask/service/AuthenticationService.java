package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;

public interface AuthenticationService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse refresh(RefreshTokenRequest refreshRequest);

    void logout(LogoutRequest logoutRequest);
}
