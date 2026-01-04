package com.epam.finaltask.exception;

import com.epam.finaltask.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ApiExceptionHandler {

    //TODO: All exception handling here

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleOAuth2AuthenticationException(
            OAuth2AuthenticationException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest().body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null)
        );
    }

    @ExceptionHandler(NotEnoughBalanceException.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughBalance(
            NotEnoughBalanceException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.NOT_ACCEPTABLE,
                ex.getMessage(),
                null)
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest().body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null)
        );
    }

    @ExceptionHandler(AlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyInUseException(
            AlreadyInUseException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.CONFLICT,
                ex.getMessage(),
                null)
        );
    }

    @ExceptionHandler({EntityNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            Exception ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null)
        );
    }

    @ExceptionHandler({ConversionFailedException.class, InvalidFormatException.class})
    public ResponseEntity<ErrorResponse> handleConversionFailed(
            Exception ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Conversion error",
                null)
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest().body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null)
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED,
                "Invalid token",
                null)
        );
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(
            ExpiredJwtException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.UNAUTHORIZED,
                "Token is expired",
                null)
        );
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

        return ResponseEntity.badRequest().body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                "Validation error",
                validationErrors)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected in {} with cause = {}",
                request.getRequestURI(), ex.getCause() != null ? ex.getCause() : "NULL", ex);

        return ResponseEntity.badRequest().body(generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                null)
        );
    }

    private ErrorResponse generateErrorResponse(String path,
                                                HttpStatus status,
                                                String message,
                                                List<ErrorResponse.ValidationError> validationErrors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .statusCode(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}
