package com.epam.finaltask.exception;

public class AlreadyInUseException extends LocalizedException {

    public AlreadyInUseException(String resourceName, String value) {
        super("error.resource.already_in_use", resourceName, value);
    }
}
