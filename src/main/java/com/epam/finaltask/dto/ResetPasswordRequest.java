package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Token not found")
    private String token;

    @NotBlank(message = "Please provide password")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @Pattern(regexp = "^\\S+$",
            message = "Password must not contain spaces")
    @Pattern(regexp = ".*[0-9].*",
            message = "Password must contain at least one digit")
    @Pattern(regexp = ".*[a-z].*",
            message = "Password must contain at least one lowercase letter")
    @Pattern(regexp = ".*[A-Z].*",
            message = "Password must contain at least one uppercase letter")
    @Pattern(regexp = ".*[!@#$%&*()+=^.-].*",
            message = "Password must contain at least one special character (!@#$%&*()+=^.-)")
    @ToString.Exclude
    private String newPassword;
}
