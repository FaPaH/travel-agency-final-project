package com.epam.finaltask.validation.annotation;

import com.epam.finaltask.validation.UniqueUsernameValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueUsernameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueUsername {

    String message() default "User with this username is already exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
