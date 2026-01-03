package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Token not found")
    private String token;

    @NotBlank(message = "Please provide password")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}
