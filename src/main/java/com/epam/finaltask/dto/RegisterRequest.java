package com.epam.finaltask.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"password"})
public class RegisterRequest {

    @NotBlank(message = "Please provide username")
    private String username;

    @NotBlank(message = "Please provide password")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Pattern(regexp = "^[+]{1}(?:[0-9\\-\\(\\)\\/\\.]\\s?){6,15}[0-9]{1}$")
    private String phoneNumber;

    @Email
    private String email;
}
