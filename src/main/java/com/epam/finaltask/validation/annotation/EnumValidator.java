package com.epam.finaltask.validation.annotation;

import com.epam.finaltask.validation.EnumValidatorConstraint;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidatorConstraint.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface EnumValidator {

    Class<? extends Enum<?>> enumClass();

    String message() default "Invalid value. Must be one of {allowedValues}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
