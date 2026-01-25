package com.epam.finaltask.service;

import com.epam.finaltask.dto.*;
import com.epam.finaltask.exception.ExpiredTokenException;
import com.epam.finaltask.exception.InvalidTokenException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.AuthProvider;
import com.epam.finaltask.model.ResetToken;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.service.impl.AuthenticationServiceImpl;
import com.epam.finaltask.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserMapper userMapper;
    @Mock
    private TokenStorageService<String> jwtTokenStorageService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ResetService resetService;

    @InjectMocks
    private AuthenticationServiceImpl authService;

    @Test
    @DisplayName("Login: Success")
    void login() {
        User user = User.builder().id(UUID.randomUUID()).build();
        when(userService.getUserByUsername("u")).thenReturn(new UserDTO());
        when(userMapper.toUser(any())).thenReturn(user);
        when(jwtUtil.generateAccessToken(user)).thenReturn("a");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("r");

        AuthResponse res = authService.login(new LoginRequest("u", "p"));
        assertThat(res.getAccessToken()).isEqualTo("a");
        verify(jwtTokenStorageService).store(user.getId().toString(), "r");
    }

    @Test
    @DisplayName("Register: Success")
    void register() {
        User user = User.builder().id(UUID.randomUUID()).build();
        when(passwordEncoder.encode("p")).thenReturn("enc");
        when(userMapper.toUserDTO(any())).thenReturn(new UserDTO());
        when(userService.saveUser(any(), eq("enc"))).thenReturn(new UserDTO());
        when(userMapper.toUser(any())).thenReturn(user);
        when(jwtUtil.generateAccessToken(user)).thenReturn("a");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("r");

        authService.register(new RegisterRequest("u", "p", "f", "l", "ph", "e"));
        verify(userService).saveUser(any(), any());
    }

    @Test
    @DisplayName("Refresh: Success")
    void refresh() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().id(uid).build();
        String token = "tok";

        when(jwtUtil.extractUsername(token)).thenReturn("u");
        when(jwtTokenStorageService.get("u")).thenReturn(token);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.extractClaim(eq(token), any())).thenReturn(uid.toString());
        when(userService.getUserById(uid)).thenReturn(new UserDTO());
        when(userMapper.toUser(any())).thenReturn(user);
        when(jwtUtil.generateAccessToken(user)).thenReturn("newA");

        authService.refresh(new RefreshTokenRequest(token));
        verify(jwtTokenStorageService).store(any(), any());
    }

    @Test
    @DisplayName("Refresh: Invalid Token")
    void refresh_Invalid() {
        when(jwtUtil.extractUsername("t")).thenReturn("u");
        when(jwtTokenStorageService.get("u")).thenReturn(null);
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("t"))).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @DisplayName("Logout: With ID")
    void logout() {
        Claims claims = mock(Claims.class);
        when(claims.get("id", String.class)).thenReturn("id");
        when(jwtUtil.extractAllClaims("t")).thenReturn(claims);
        authService.logout(new LogoutRequest("t"));
        verify(jwtTokenStorageService).revoke("id");
    }

    @Test
    @DisplayName("Reset Password")
    void resetPassword() {
        ResetToken rt = new ResetToken();
        rt.setToken("t");
        rt.setUserDTO(UserDTO.builder().id("uid").build());

        when(resetService.getResetToken("t")).thenReturn(rt);
        when(passwordEncoder.encode("np")).thenReturn("enc");

        authService.resetPassword(new ResetPasswordRequest("t", "np"));

        verify(userService).changePassword(any(), eq("enc"));
        verify(jwtTokenStorageService).revoke("uid");
    }

    @Test
    @DisplayName("Refresh: Token Expired")
    void refresh_Expired() {
        // Arrange
        String token = "expired-token";
        when(jwtUtil.extractUsername(token)).thenReturn("user");
        when(jwtTokenStorageService.get("user")).thenReturn(token);
        when(jwtUtil.isTokenExpired(token)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(token)))
                .isInstanceOf(ExpiredTokenException.class);
    }

    @Test
    @DisplayName("Logout: Without ID in claims (Should skip revoke)")
    void logout_NoId_ShouldOnlyClearContext() {
        // Arrange
        String token = "token-without-id";
        Claims claims = mock(Claims.class);
        when(claims.get("id", String.class)).thenReturn(null);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        // Act
        authService.logout(new LogoutRequest(token));

        // Assert
        verify(jwtTokenStorageService, never()).revoke(anyString());
    }

    @Test
    @DisplayName("Register: Verification of user fields mapping")
    void register_CheckFieldsMapping() {
        // Arrange
        RegisterRequest req = new RegisterRequest("john_doe", "pass", "John", "Doe", "123", "j@e.com");
        UUID generatedId = UUID.randomUUID();
        User savedUser = User.builder().id(generatedId).username("john_doe").build();
        UserDTO userDTO = new UserDTO();

        ArgumentCaptor<UserDTO> userDtoCaptor = ArgumentCaptor.forClass(UserDTO.class);

        when(passwordEncoder.encode("pass")).thenReturn("encoded_pass");
        when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);
        when(userService.saveUser(any(), eq("encoded_pass"))).thenReturn(userDTO);
        when(userMapper.toUser(userDTO)).thenReturn(savedUser);

        when(jwtUtil.generateAccessToken(any())).thenReturn("at");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("rt");

        // Act
        authService.register(req);

        // Assert
        verify(userMapper).toUserDTO(argThat(user ->
                user.getUsername().equals("john_doe") &&
                        user.getRole() == Role.USER &&
                        user.getAuthProvider() == AuthProvider.LOCAL &&
                        user.isActive()
        ));
    }
}
