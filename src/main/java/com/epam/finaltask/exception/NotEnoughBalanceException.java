package com.epam.finaltask.exception;

import java.math.BigDecimal;

public class NotEnoughBalanceException extends LocalizedException {

    public NotEnoughBalanceException(BigDecimal current, BigDecimal required) {
        super("error.balance.not_enough", current, required);
    }
}
