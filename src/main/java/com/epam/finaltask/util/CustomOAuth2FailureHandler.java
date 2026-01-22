package com.epam.finaltask.util;

import com.epam.finaltask.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String path = request.getRequestURI();

        String errorCode = determineErrorCode(exception);

        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            String translatedMessage = messageSource.getMessage(
                    errorCode,
                    null,
                    "Authentication Failed",
                    LocaleContextHolder.getLocale()
            );

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .statusCode(HttpStatus.UNAUTHORIZED.value())
                    .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                    .message(translatedMessage)
                    .path(path)
                    .validationErrors(null)
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        String redirectUrl = "/auth/sign-in?error=" +
                URLEncoder.encode(errorCode, StandardCharsets.UTF_8);

        if (request.getHeader("HX-Request") != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("HX-Redirect", redirectUrl);
            return;
        }

        response.sendRedirect(redirectUrl);
    }

    private String determineErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            return oauthEx.getError().getErrorCode();
        } else if (exception instanceof DisabledException) {
            return "error.auth.account_disabled";
        } else {
            return "error.auth.general";
        }
    }
}
