package com.epam.finaltask.exception;

import com.epam.finaltask.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
@Slf4j
public class UIExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(LocalizedException.class)
    public String handleLocalizedException(LocalizedException ex,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           Model model,
                                           Locale locale) {

        HttpStatus status = getStatusForException(ex);
        return returnErrorAlert(ex.getMessageKey(), ex.getArgs(), request, response, model, status, locale, null);
    }

    @ExceptionHandler({
            DisabledException.class,
            LockedException.class,
            BadCredentialsException.class,
            InvalidTokenException.class,
            OAuth2AuthenticationException.class,
            ConversionFailedException.class,
            InvalidFormatException.class,
            InternalAuthenticationServiceException.class,
            AuthenticationException.class
    })
    public String handleStandardExceptions(Exception ex,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           Model model,
                                           Locale locale) {

        String key = "error.general";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex instanceof BadCredentialsException) {
            key = "error.auth.bad_credentials";
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof DisabledException || ex instanceof LockedException) {
            key = "error.auth.disabled";
            status = HttpStatus.LOCKED;
        } else if (ex instanceof AuthenticationException) {
            key = "error.auth.general";
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof EntityNotFoundException || ex instanceof NoResourceFoundException) {
            key = "error.resource.not_found_generic";
            status = HttpStatus.NOT_FOUND;
        }

        return returnErrorAlert(key, null, request, response, model, status, locale, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex,
                                             HttpServletRequest request,
                                             HttpServletResponse response,
                                             Model model,
                                             Locale locale) {

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        return returnErrorAlert(
                "error.validation.failed",
                null,
                request,
                response,
                model,
                HttpStatus.BAD_REQUEST,
                locale,
                validationErrors
        );
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex,
                                         HttpServletRequest request,
                                         Model model,
                                         HttpServletResponse response,
                                         Locale locale) {

        log.error("Unexpected error in {} with cause = {}",
                request.getRequestURI(), ex.getCause() != null ? ex.getCause() : "NULL", ex);

        String message = messageSource.getMessage(
                "error.server.internal",
                null,
                "Unexpected internal error",
                locale
        );

        ErrorResponse errorResponse = generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                null);

        model.addAttribute("errorResponse", errorResponse);

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setHeader("HX-Retarget", "body");
        response.setHeader("HX-Reswap", "innerHTML");

        return "error/500";
    }

    private String returnErrorAlert(String key,
                                    Object[] args,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Model model,
                                    HttpStatus status,
                                    Locale locale,
                                    List<ErrorResponse.ValidationError> validationErrors) {

        String translatedMessage = messageSource.getMessage(key, args, "Error: " + key, locale);

        ErrorResponse errorResponse = generateErrorResponse(
                request.getRequestURI(),
                status,
                translatedMessage,
                validationErrors
        );

        model.addAttribute("errorResponse", errorResponse);

        response.setStatus(status.value());
        response.setHeader("HX-Retarget", "#alerts-container");
        response.setHeader("HX-Reswap", "innerHTML");

        return "fragments/common :: error-alert-fragment";
    }

    private HttpStatus getStatusForException(Exception ex) {
        if (ex instanceof NotEnoughBalanceException) return HttpStatus.NOT_ACCEPTABLE;
        if (ex instanceof AlreadyInUseException) return HttpStatus.CONFLICT;
        if (ex instanceof InvalidTokenException ||
                ex instanceof ExpiredTokenException) return HttpStatus.UNAUTHORIZED;
        if (ex instanceof ResourceNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof DisabledException) return HttpStatus.LOCKED;
        if (ex instanceof BadCredentialsException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof OAuth2AuthenticationException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof ConversionFailedException) return HttpStatus.UNPROCESSABLE_ENTITY;
        if (ex instanceof InvalidFormatException) return HttpStatus.UNPROCESSABLE_ENTITY;
        if (ex instanceof AuthenticationException) return HttpStatus.UNAUTHORIZED;

        return HttpStatus.BAD_REQUEST;
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
