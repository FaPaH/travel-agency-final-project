package com.epam.finaltask.exception;

import com.epam.finaltask.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class ApiExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(LocalizedException.class)
    public ResponseEntity<ErrorResponse> handleLocalizedException(LocalizedException ex, HttpServletRequest request) {
        HttpStatus status = getStatusForException(ex);
        return buildResponse(status, ex.getMessageKey(), ex.getArgs(), request);
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            DisabledException.class,
            OAuth2AuthenticationException.class,
            AuthenticationException.class,
            JwtException.class,
            ExpiredJwtException.class,
            InvalidTokenException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthExceptions(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String key = "error.auth.general";

        if (ex instanceof BadCredentialsException) {
            key = "error.auth.bad_credentials";
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof DisabledException) {
            key = "error.auth.disabled";
            status = HttpStatus.LOCKED;
        } else if (ex instanceof ExpiredJwtException) {
            key = "error.token.expired";
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof JwtException || ex instanceof InvalidTokenException) {
            key = "error.token.invalid";
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof OAuth2AuthenticationException) {
            key = "error.auth.oauth2";
            status = HttpStatus.BAD_REQUEST;
        }

        return buildResponse(status, key, null, request);
    }

    @ExceptionHandler({EntityNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "error.resource.not_found_generic", null, request);
    }

    @ExceptionHandler({ConversionFailedException.class, InvalidFormatException.class})
    public ResponseEntity<ErrorResponse> handleUnprocessable(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "error.conversion.failed", null, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "error.validation.failed", null, request, validationErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error in {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error.server.internal", null, request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String key, Object[] args, HttpServletRequest request) {
        return buildResponse(status, key, args, request, null);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                                                        String key,
                                                        Object[] args,
                                                        HttpServletRequest request,
                                                        List<ErrorResponse.ValidationError> errors) {

        Locale locale = LocaleContextHolder.getLocale();


        String message = messageSource.getMessage(key, args, "Error: " + key, locale);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    private HttpStatus getStatusForException(LocalizedException ex) {
        if (ex instanceof NotEnoughBalanceException) return HttpStatus.NOT_ACCEPTABLE;
        if (ex instanceof AlreadyInUseException) return HttpStatus.CONFLICT;
        return HttpStatus.BAD_REQUEST;
    }
}