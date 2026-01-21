package com.epam.finaltask.validation.annotation;

import com.epam.finaltask.validation.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {

    String message() default "User with this email is already exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
