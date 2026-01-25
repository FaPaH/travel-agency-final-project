package com.epam.finaltask.service;

import com.epam.finaltask.model.User;
import com.epam.finaltask.service.impl.SecurityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    private SecurityService securityService;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        securityService = new SecurityService();
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    @DisplayName("isUserObject: Should return true when IDs match")
    void isUserObject_IdsMatch_ReturnsTrue() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        User user = User.builder().id(uuid).build();

        setupMockContext(user);

        // Act
        boolean result = securityService.isUserObject(uuid.toString());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isUserObject: Should return false when IDs do not match")
    void isUserObject_IdsMismatch_ReturnsFalse() {
        // Arrange
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        User user = User.builder().id(currentUserId).build();

        setupMockContext(user);

        // Act
        boolean result = securityService.isUserObject(otherUserId.toString());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isUserObject: Should return false when authentication is null")
    void isUserObject_NullAuthentication_ReturnsFalse() {
        // Arrange
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(context);

        // Act
        boolean result = securityService.isUserObject(UUID.randomUUID().toString());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isUserObject: Should return false when principal is not a User object")
    void isUserObject_PrincipalNotUser_ReturnsFalse() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("Not A User Object");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(context);

        // Act
        boolean result = securityService.isUserObject(UUID.randomUUID().toString());

        // Assert
        assertThat(result).isFalse();
    }

    private void setupMockContext(Object principal) {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(context);
    }
}
