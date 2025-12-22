package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

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

        if (jwtUtil.isTokenExpired(refreshRequest.getRefreshToken())) {
            throw new RuntimeException("Refresh token expired, please log in again");
        }

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
    public void logout(LogoutRequest logoutRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            new SecurityContextLogoutHandler().logout(request, response, auth);

            String id = jwtUtil.extractAllClaims(logoutRequest.getRefreshToken()).get("id",String.class);

            tokenStorageService.revokeRefreshToken(id);
        }
    }
}
