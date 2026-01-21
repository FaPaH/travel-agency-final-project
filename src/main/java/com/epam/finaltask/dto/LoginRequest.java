package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "{validation.auth.username.required}")
    private String username;

    @NotBlank(message = "{validation.auth.password.required}")
    @ToString.Exclude
    private String password;
}
