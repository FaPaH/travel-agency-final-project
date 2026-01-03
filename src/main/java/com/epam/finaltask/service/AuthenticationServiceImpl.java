package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.exception.InvalidTokenException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    //TODO: rest Voucher controller/service,
    // exception handling for rest,
    // logging

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenStorageService<String> JwtTokenStorageService;

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
        if (JwtTokenStorageService.get(jwtUtil.extractUsername(refreshRequest.getRefreshToken())) == null
                || jwtUtil.isTokenExpired(refreshRequest.getRefreshToken())) {
            throw new InvalidTokenException("Token has expired, please login again");
        }

        User user = userMapper.toUser(userService.getUserByUsername(jwtUtil.extractUsername(refreshRequest.getRefreshToken())));

        return generateTokensAndStore(user);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        String id = jwtUtil.extractAllClaims(logoutRequest.getRefreshToken()).get("id", String.class);

        if (id != null) {
            JwtTokenStorageService.revoke(id);
        }

        SecurityContextHolder.clearContext();
    }

    @Override
    public AuthResponse generateTokensAndStore(User user) {
        AuthResponse authResponse = generateTokens(user);

        JwtTokenStorageService.revoke(user.getId().toString());
        JwtTokenStorageService.store(user.getId().toString(), authResponse.getRefreshToken());

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
