package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    //TODO: OAuth implementation, rest User controller/service, rest Voucher controller/service

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final TokenStorageService tokenStorageService;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        User user = (User) userService
                .userDetailsService()
                .loadUserByUsername(loginRequest.getUsername());

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        if (tokenStorageService.getRefreshToken(user.getId().toString()) == null) {
            tokenStorageService.storeRefreshToken(user.getId().toString(), refreshToken);
        } else {
            //delete old token
            tokenStorageService.revokeRefreshToken(user.getId().toString());
            //add new token
            tokenStorageService.storeRefreshToken(user.getId().toString(), refreshToken);
        }

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

        UserDTO newUser = userService.register(userMapper.toUserDTO(user));

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        tokenStorageService.storeRefreshToken(newUser.getId(), refreshToken);

        return new AuthResponse(jwtToken, refreshToken);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest refreshRequest) {

        User user = userMapper.toUser(userService.getUserByUsername(jwtUtil.extractUsername(refreshRequest.getRefreshToken())));
        System.out.println("before ex");
        if (jwtUtil.isTokenExpired(refreshRequest.getRefreshToken())
                || tokenStorageService.getRefreshToken(user.getId().toString()) == null) {
            System.out.println("exception");
            throw new RuntimeException("Please log in again");
        }

        String jwtToken = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        tokenStorageService.revokeRefreshToken(user.getId().toString());
        tokenStorageService.storeRefreshToken(user.getId().toString(), refreshToken);

        return new AuthResponse(jwtToken, refreshToken);
    }

    @Override
    public void logout(LogoutRequest logoutRequest) {

        String id = jwtUtil.extractAllClaims(logoutRequest.getRefreshToken()).get("id",String.class);

        if (id != null) {
            tokenStorageService.revokeRefreshToken(id);
        }
    }
}
