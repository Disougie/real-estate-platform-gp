package com.disougie.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface Password {
	
	String message() default "Invalid password length";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};

}
