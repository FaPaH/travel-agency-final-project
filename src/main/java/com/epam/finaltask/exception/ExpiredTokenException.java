package com.epam.finaltask.exception;

public class ExpiredTokenException extends LocalizedException {

    public ExpiredTokenException() {
        super("error.token.expired");
    }
}
