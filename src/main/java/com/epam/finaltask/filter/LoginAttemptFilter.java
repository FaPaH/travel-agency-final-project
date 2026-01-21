package com.epam.finaltask.filter;

import com.epam.finaltask.dto.ErrorResponse;
import com.epam.finaltask.service.AttemptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptFilter extends OncePerRequestFilter {

    private final AttemptService attemptService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("Starting Login Attempts Filter");

        if (isLoginRequest(request)) {

            String ip = getClientIP(request);

            if (attemptService.isBlocked(ip)) {
                handleBlockedResponse(request, response);
                return;
            }
        }

        log.debug("End Login Attempts Filter");

        filterChain.doFilter(request, response);
    }

    private void handleBlockedResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Locale locale = localeResolver.resolveLocale(request);

        String message = messageSource.getMessage(
                "error.auth.blocked",
                null,
                "Too many failed attempts. You are blocked.",
                locale
        );

        String path = request.getRequestURI();

        if (path.startsWith("/api/")) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .statusCode(HttpStatus.TOO_MANY_REQUESTS.value())
                    .error("Too Many Requests")
                    .message(message)
                    .path(path)
                    .validationErrors(null)
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String redirectUrl = "/auth/sign-in?error=" + encodedMsg;

        if (request.getHeader("HX-Request") != null) {
            response.setStatus(HttpStatus.OK.value());
            response.setHeader("HX-Redirect", redirectUrl);
        } else {
            response.sendRedirect(redirectUrl);
        }
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return ("/auth/perform_login".equals(request.getRequestURI()))
                && "POST".equalsIgnoreCase(request.getMethod());
    }

    private String getClientIP(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(h -> h.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }
}