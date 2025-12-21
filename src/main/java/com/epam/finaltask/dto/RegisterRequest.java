package com.epam.finaltask.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class RegisterRequest {

    private String username;

    private String password;

    private String phoneNumber;

    private String email;
}
