package com.epam.finaltask.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Please provide username")
    @Size(min = 2, max = 16, message = "Min length 2, max 16")
    private String username;

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
    private String password;

    @Pattern(regexp = "^$|^(?=.{2,16}$)[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
            message = "Invalid name format (2-16 chars)")
    private String firstName;

    @Pattern(regexp = "^$|^(?=.{2,16}$)[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
            message = "Invalid name format (2-16 chars)")
    private String lastName;

    @Pattern(regexp = "^$|^[+]{1}(?:[0-9\\-\\(\\)\\/\\.]\\s?){6,15}[0-9]{1}$",
            message = "Invalid phone format")
    private String phoneNumber;

    @Pattern(regexp = "^$|^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,10}$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Invalid mail format")
    private String email;
}
