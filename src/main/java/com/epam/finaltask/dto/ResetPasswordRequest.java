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

    @NotBlank(message = "{validation.token.required}")
    private String token;

    @NotBlank(message = "{validation.auth.password.required}")
    @Size(min = 8, max = 20, message = "{validation.password.size}")
    @Pattern(regexp = "^\\S+$",
            message = "{validation.password.no_spaces}")
    @Pattern(regexp = ".*[0-9].*",
            message = "{validation.password.digit}")
    @Pattern(regexp = ".*[a-z].*",
            message = "{validation.password.lowercase}")
    @Pattern(regexp = ".*[A-Z].*",
            message = "{validation.password.uppercase}")
    @Pattern(regexp = ".*[!@#$%&*()+=^.-].*",
            message = "{validation.password.special}")
    @ToString.Exclude
    private String newPassword;
}
