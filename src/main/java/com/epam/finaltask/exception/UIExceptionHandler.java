package com.epam.finaltask.exception;

import com.epam.finaltask.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
@Slf4j
public class UIExceptionHandler {

    @ExceptionHandler({
            AlreadyInUseException.class,
            NotEnoughBalanceException.class,
            DisabledException.class,
            BadCredentialsException.class,
            InvalidTokenException.class,
            OAuth2AuthenticationException.class,
            ConversionFailedException.class,
            InvalidFormatException.class,
            InternalAuthenticationServiceException.class
    })
    public String handleBusinessExceptions(Exception ex,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           Model model) {

        HttpStatus status = getStatusForException(ex);

        String message = ex.getMessage();

        if (ex instanceof InternalAuthenticationServiceException) {
            message = "Invalid username or password";
        }

        return returnErrorAlert(new Exception(message), request, response, model, status);
    }

    @ExceptionHandler({JwtException.class, ExpiredJwtException.class})
    public String handleJwtExceptions(Exception ex,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      Model model) {
        return returnErrorAlert(ex, request, response, model, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({EntityNotFoundException.class, NoResourceFoundException.class})
    public String handleNotFoundExceptions(Exception ex,
                                           HttpServletRequest request,
                                           HttpServletResponse response,
                                           Model model) {
        return returnErrorAlert(ex, request, response, model, HttpStatus.NOT_FOUND);

//        ErrorResponse errorResponse = generateErrorResponse(request.getRequestURI(), HttpStatus.NOT_FOUND, ex.getMessage(), null);
//        model.addAttribute("errorResponse", errorResponse);
//        response.setStatus(HttpStatus.NOT_FOUND.value());
//        response.setHeader("HX-Retarget", "body");
//        response.setHeader("HX-Reswap", "innerHTML");
//        return "error/404";

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(MethodArgumentNotValidException ex,
                                             HttpServletRequest request,
                                             HttpServletResponse response,
                                             Model model) {

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .toList();

        ErrorResponse errorResponse = generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                validationErrors
        );

        model.addAttribute("errorResponse", errorResponse);

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setHeader("HX-Retarget", "#alerts-container");
        response.setHeader("HX-Reswap", "innerHTML");

        return "fragments/common :: error-alert-fragment";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex,
                                         HttpServletRequest request,
                                         Model model,
                                         HttpServletResponse response) {
        log.error("Unexpected error in {} with cause = {}",
                request.getRequestURI(), ex.getCause() != null ? ex.getCause() : "NULL", ex);

        ErrorResponse errorResponse = generateErrorResponse(
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                null);

        model.addAttribute("errorResponse", errorResponse);

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        response.setHeader("HX-Retarget", "body");
        response.setHeader("HX-Reswap", "innerHTML");

        return "error/500";
    }

    private String returnErrorAlert(Exception ex,
                                    HttpServletRequest request,
                                    HttpServletResponse response,
                                    Model model,
                                    HttpStatus status) {

        ErrorResponse errorResponse = generateErrorResponse(
                request.getRequestURI(),
                status,
                ex.getMessage(),
                null);

        model.addAttribute("errorResponse", errorResponse);

        response.setStatus(status.value());

        response.setHeader("HX-Retarget", "#alerts-container");
        response.setHeader("HX-Reswap", "innerHTML");

        return "fragments/common :: error-alert-fragment";
    }

    private HttpStatus getStatusForException(Exception ex) {
        if (ex instanceof DisabledException) return HttpStatus.LOCKED; // 423
        if (ex instanceof NotEnoughBalanceException) return HttpStatus.NOT_ACCEPTABLE; // 406
        if (ex instanceof AlreadyInUseException) return HttpStatus.CONFLICT; // 409
        if (ex instanceof BadCredentialsException) return HttpStatus.BAD_REQUEST; // 400
        if (ex instanceof OAuth2AuthenticationException) return HttpStatus.BAD_REQUEST; // 400
        if (ex instanceof InvalidTokenException) return HttpStatus.BAD_REQUEST; // 400
        if (ex instanceof ConversionFailedException) return HttpStatus.UNPROCESSABLE_ENTITY; // 422
        if (ex instanceof InvalidFormatException) return HttpStatus.UNPROCESSABLE_ENTITY; // 422

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
