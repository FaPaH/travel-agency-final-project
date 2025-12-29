package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    //TODO: OAuth implementation (check),
    // password reset (check),
    // rest User controller/service (current),
    // rest Voucher controller/service,
    // exception handling for rest,
    // logging

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenStorageService<String> refreshTokenStorageService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        User user = (User) userService
                .userDetailsService()
                .loadUserByUsername(loginRequest.getUsername());

        return generateTokensAndStore(user);
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        User newUser = userMapper.toUser(userService.register(userMapper.toUserDTO(user)));

        return generateTokensAndStore(newUser);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest refreshRequest) {

        User user = userMapper.toUser(userService.getUserByUsername(jwtUtil.extractUsername(refreshRequest.getRefreshToken())));

        if (jwtUtil.isTokenExpired(refreshRequest.getRefreshToken())
                || refreshTokenStorageService.get(user.getId().toString()) == null) {
            throw new RuntimeException("Please log in again");
        }

        return generateTokensAndStore(user);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {

        String id = jwtUtil.extractAllClaims(logoutRequest.getRefreshToken()).get("id",String.class);

        if (id != null) {
            refreshTokenStorageService.revoke(id);
        }
    }

    @Override
    public AuthResponse generateTokensAndStore(User user) {
        AuthResponse authResponse = generateTokens(user);

        refreshTokenStorageService.revoke(user.getId().toString());
        refreshTokenStorageService.store(user.getId().toString(), authResponse.getRefreshToken());

        return authResponse;
    }

    private AuthResponse generateTokens(User user) {
        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}
