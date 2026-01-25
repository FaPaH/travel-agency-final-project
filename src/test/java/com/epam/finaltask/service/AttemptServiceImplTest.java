package com.epam.finaltask.service;

import com.epam.finaltask.service.impl.AttemptServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttemptServiceImplTest {

    @Mock
    private TokenStorageService<Integer> failedAttemptStorage;
    @InjectMocks
    private AttemptServiceImpl attemptService;

    @Test
    void track_Null_Init() {
        when(failedAttemptStorage.get("login:fail:ip")).thenReturn(null);
        attemptService.track("ip");
        verify(failedAttemptStorage).store("login:fail:ip", 1);
    }

    @Test
    void track_Increment() {
        when(failedAttemptStorage.get("login:fail:ip")).thenReturn(2);
        attemptService.track("ip");
        verify(failedAttemptStorage).store("login:fail:ip", 3);
    }

    @Test
    void isBlocked_True() {
        when(failedAttemptStorage.get("login:fail:ip")).thenReturn(5);
        assertThat(attemptService.isBlocked("ip")).isTrue();
    }

    @Test
    void isBlocked_False_Null() {
        when(failedAttemptStorage.get("login:fail:ip")).thenReturn(null);
        assertThat(attemptService.isBlocked("ip")).isFalse();
    }

    @Test
    void clearBlocked() {
        attemptService.clearBlocked("ip");
        verify(failedAttemptStorage).revoke("ip");
    }
}
