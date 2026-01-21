package com.epam.finaltask.service.impl;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.exception.ExpiredTokenException;
import com.epam.finaltask.exception.InvalidTokenException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.ResetToken;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.ResetService;
import com.epam.finaltask.service.TokenStorageService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenStorageService<String> jwtTokenStorageService;
    private final PasswordEncoder passwordEncoder;
    private final ResetService resetService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        User user = userMapper.toUser(userService.getUserByUsername(loginRequest.getUsername()));

        return generateTokensAndStore(user);
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .active(true)
                .build();

        User newUser = userMapper.toUser(userService.saveUser(userMapper.toUserDTO(user),
                passwordEncoder.encode(registerRequest.getPassword())));

        return generateTokensAndStore(newUser);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest refreshRequest) {
        if (jwtTokenStorageService.get(jwtUtil.extractUsername(refreshRequest.getRefreshToken())) == null) {
            throw new InvalidTokenException();
        }
        if (jwtUtil.isTokenExpired(refreshRequest.getRefreshToken())) {
            throw new ExpiredTokenException();
        }

        User user = userMapper.toUser(
                userService.getUserById(
                        UUID.fromString(
                            jwtUtil.extractClaim(
                                    refreshRequest.getRefreshToken(),
                                    claims -> claims.get("id", String.class)
                            )
                        )
                )
        );

        return generateTokensAndStore(user);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        String id = jwtUtil.extractAllClaims(logoutRequest.getRefreshToken()).get("id", String.class);

        if (id != null) {
            jwtTokenStorageService.revoke(id);
        }

        SecurityContextHolder.clearContext();
    }

    @Override
    public AuthResponse generateTokensAndStore(User user) {
        AuthResponse authResponse = generateTokens(user);

        jwtTokenStorageService.revoke(user.getId().toString());
        jwtTokenStorageService.store(user.getId().toString(), authResponse.getRefreshToken());

        return authResponse;
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {

        ResetToken tokenRecord = resetService.getResetToken(resetPasswordRequest.getToken());
        UserDTO user = tokenRecord.getUserDTO();

        userService.changePassword(user, passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        resetService.removeResetToken(tokenRecord.getToken());
        jwtTokenStorageService.revoke(user.getId());
    }

    private AuthResponse generateTokens(User user) {
        String jwtToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}
