package com.epam.finaltask.dto;

import com.epam.finaltask.validation.annotation.UniqueEmail;
import com.epam.finaltask.validation.annotation.UniqueUsername;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{validation.auth.username.required}")
    @Size(min = 2, max = 16, message = "{validation.user.username.size}")
    @UniqueUsername(message = "{validation.username.exists}")
    private String username;

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
    private String password;

    @Pattern(regexp = "^$|^(?=.{2,16}$)[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
            message = "{validation.user.name.format}")
    @Size(min = 2, max = 16, message = "{validation.user.name.format}")
    private String firstName;

    @Pattern(regexp = "^$|^(?=.{2,16}$)[a-zA-Zа-яА-Я]+(?:[\\s'-][a-zA-Zа-яА-Я]+)*$",
            message = "{validation.user.name.format}")
    @Size(min = 2, max = 16, message = "{validation.user.name.format}")
    private String lastName;

    @Pattern(regexp = "^$|^[+]{1}(?:[0-9\\-\\(\\)\\/\\.]\\s?){6,15}[0-9]{1}$",
            message = "{validation.user.phone.format}")
    private String phoneNumber;

    @Pattern(regexp = "^$|^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,10}$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "{validation.user.email.format}")
    @UniqueEmail(message = "{validation.email.exists}")
    private String email;
}
