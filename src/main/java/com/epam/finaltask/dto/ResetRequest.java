package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetRequest {

    @NotBlank(message = "{validation.user.email.required}")
    @Pattern(regexp = "^$|^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,10}$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "{validation.user.email.format}")
    private String email;
}
