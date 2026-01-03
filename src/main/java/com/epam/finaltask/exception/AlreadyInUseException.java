package com.epam.finaltask.exception;

public class AlreadyInUseException extends RuntimeException {

    public AlreadyInUseException(String msg) {
        super(msg);
    }
}
