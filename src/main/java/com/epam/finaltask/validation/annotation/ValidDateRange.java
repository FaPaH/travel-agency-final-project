package com.epam.finaltask.validation.annotation;

import com.epam.finaltask.validation.DateRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
public @interface ValidDateRange {
    String message() default "Eviction date must be after arrival date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
