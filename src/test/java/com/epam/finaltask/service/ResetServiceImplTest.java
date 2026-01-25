package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.model.ResetToken;
import com.epam.finaltask.service.impl.ResetServiceImpl;
import com.epam.finaltask.util.JwtProperties;
import com.epam.finaltask.util.ResetTokenUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private TokenStorageService<ResetToken> resetTokenStorageService;
    @Mock
    private MailService mailService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private ResetTokenUtil resetTokenUtil;

    @InjectMocks
    private ResetServiceImpl resetService;

    @Test
    @DisplayName("proceedReset: should generate API URL and store token with correct expiration")
    void proceedReset_ApiRequest_ShouldStoreAndSendMail() {
        // Arrange
        String email = "test@example.com";
        String token = "secret-token";
        UserDTO userDTO = UserDTO.builder().email(email).build();

        when(userService.getUserByEmail(email)).thenReturn(userDTO);
        when(resetTokenUtil.generateResetToken()).thenReturn(token);
        when(jwtProperties.getExpiration()).thenReturn(3600L); // 1 hour

        // Act
        resetService.proceedReset(email, true);

        // Assert
        ArgumentCaptor<ResetToken> tokenCaptor = ArgumentCaptor.forClass(ResetToken.class);
        verify(resetTokenStorageService).store(eq(token), tokenCaptor.capture());

        ResetToken capturedToken = tokenCaptor.getValue();
        assertThat(capturedToken.getToken()).isEqualTo(token);
        assertThat(capturedToken.getUserDTO()).isEqualTo(userDTO);
        assertThat(capturedToken.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(59));

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendTextMail(eq(email), eq(token), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("/api/auth/reset-password/validate?token=" + token);
    }

    @Test
    @DisplayName("proceedReset: should generate browser URL when isApi is false")
    void proceedReset_BrowserRequest_ShouldUseNonApiUrl() {
        // Arrange
        when(userService.getUserByEmail(any())).thenReturn(UserDTO.builder().email("e").build());
        when(resetTokenUtil.generateResetToken()).thenReturn("t");
        when(jwtProperties.getExpiration()).thenReturn(60L);

        // Act
        resetService.proceedReset("e", false);

        // Assert
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendTextMail(any(), any(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains("/auth/reset-password/validate?token=t");
        assertThat(bodyCaptor.getValue()).doesNotContain("/api/auth/");
    }

    @Test
    @DisplayName("validateToken: should return true for valid non-expired token")
    void validateToken_Valid_ReturnsTrue() {
        // Arrange
        ResetToken t = ResetToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(resetTokenStorageService.get("valid-t")).thenReturn(t);

        // Act & Assert
        assertThat(resetService.validateToken("valid-t")).isTrue();
    }

    @Test
    @DisplayName("validateToken: should return false when token is not in storage")
    void validateToken_NotFound_ReturnsFalse() {
        // Arrange
        when(resetTokenStorageService.get("missing-t")).thenReturn(null);

        // Act & Assert
        assertThat(resetService.validateToken("missing-t")).isFalse();
    }

    @Test
    @DisplayName("validateToken: should return false when token is expired")
    void validateToken_Expired_ReturnsFalse() {
        // Arrange
        ResetToken t = ResetToken.builder()
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .build();
        when(resetTokenStorageService.get("expired-t")).thenReturn(t);

        // Act & Assert
        assertThat(resetService.validateToken("expired-t")).isFalse();
    }

    @Test
    @DisplayName("getResetToken: should fetch token from storage")
    void getResetToken_ShouldCallStorage() {
        // Act
        resetService.getResetToken("t");

        // Assert
        verify(resetTokenStorageService).get("t");
    }

    @Test
    @DisplayName("removeResetToken: should revoke token from storage")
    void removeResetToken_ShouldCallRevoke() {
        // Act
        resetService.removeResetToken("t");

        // Assert
        verify(resetTokenStorageService).revoke("t");
    }
}
