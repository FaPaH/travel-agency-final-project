package com.epam.finaltask.exception;

public class NotEnoughBalanceException extends RuntimeException {

    public NotEnoughBalanceException(String msg) {
        super(msg);
    }
}
