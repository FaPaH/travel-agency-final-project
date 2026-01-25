package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
