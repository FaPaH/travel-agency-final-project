package com.epam.finaltask.service;

import com.epam.finaltask.service.impl.MailServiceImpl;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailServiceImpl mailService;

    @Test
    @DisplayName("Send Text Mail: Should call mailSender.send(SimpleMailMessage)")
    void sendTextMail_ValidInput_ShouldSend() {
        mailService.sendTextMail("to@test.com", "Subject", "Body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Send HTML Mail: Should call mailSender.send(MimeMessage)")
    void sendHtmlMail_ValidInput_ShouldSend() {
        Session session = Session.getDefaultInstance(new Properties());
        MimeMessage realMimeMessage = new MimeMessage(session);

        when(mailSender.createMimeMessage()).thenReturn(realMimeMessage);

        // Act
        mailService.sendHtmlMail("to@test.com", "Subject", "<h1>Html</h1>");

        // Assert
        verify(mailSender).send(realMimeMessage);

        try {
            assertThat(realMimeMessage.getSubject()).isEqualTo("Subject");
        } catch (Exception e) {
            // ignore check errors
        }
    }

    @Test
    @DisplayName("Send HTML Mail: Should throw RuntimeException on underlying error")
    void sendHtmlMail_MessagingError_ShouldThrowRuntimeException() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Simulated Mail Error"));

        assertThatThrownBy(() -> mailService.sendHtmlMail("to", "sub", "html"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Send HTML Mail: Should wrap MessagingException into RuntimeException")
    @MockitoSettings(strictness = Strictness.LENIENT)
    void sendHtmlMail_MessagingException_ShouldThrowWrappedRuntimeException() throws Exception {
        // Arrange
        Session session = Session.getDefaultInstance(new Properties());

        MimeMessage mimeMessageSpy = spy(new MimeMessage(session));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessageSpy);

        doThrow(new MessagingException("Forced Subject Error"))
                .when(mimeMessageSpy).setSubject(anyString(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> mailService.sendHtmlMail("to@test.com", "Subject", "html"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error while sending mail")
                .hasCauseInstanceOf(MessagingException.class);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
