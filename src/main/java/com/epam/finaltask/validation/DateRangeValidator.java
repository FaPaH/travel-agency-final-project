package com.epam.finaltask.validation;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.validation.annotation.ValidDateRange;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (!(value instanceof VoucherDTO dto)) {
            return true;
        }

        if (dto.getArrivalDate() == null || dto.getEvictionDate() == null) {
            return true;
        }

        boolean isValid = dto.getEvictionDate().isAfter(dto.getArrivalDate());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("evictionDate")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
