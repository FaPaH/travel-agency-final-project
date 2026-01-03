package com.epam.finaltask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private int statusCode;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    private String path;
    private String traceId;

    private List<ValidationError> validationErrors;

    @Data
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
