package com.epam.finaltask.exception;

public class InvalidTokenException extends LocalizedException {

    public InvalidTokenException() {
        super("error.token.invalid");
    }
}
