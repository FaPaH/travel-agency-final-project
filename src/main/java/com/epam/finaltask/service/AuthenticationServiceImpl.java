package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    //TODO: Implement cashing for refresh token

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        var user = userService
                .userDetailsService()
                .loadUserByUsername(loginRequest.getUsername());

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return new AuthResponse(jwtToken, refreshToken);
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(Role.USER)
                .build();

        userService.register(userMapper.toUserDTO(user));

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return new AuthResponse(jwtToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest refreshRequest) {

        User user = userMapper.toUser(userService.getUserByUsername(jwtUtil.extractUsername(refreshRequest.getRefreshToken())));

        if (jwtUtil.isTokenExpired(refreshRequest.getRefreshToken())) {
            throw new RuntimeException("Refresh token expired");
        }

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return new AuthResponse(jwtToken, refreshToken);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        //TODO: Cache delete
        SecurityContextHolder.clearContext();
    }
}
