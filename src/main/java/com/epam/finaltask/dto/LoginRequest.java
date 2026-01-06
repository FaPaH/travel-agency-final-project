package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"password"})
public class LoginRequest {

    @NotBlank(message = "Please provide username")
    private String username;

    @NotBlank(message = "Please provide password")
    private String password;
}
