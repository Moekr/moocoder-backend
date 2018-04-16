package com.moekr.aes.util.validate;

import com.moekr.aes.util.validate.validator.FieldEqualsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = FieldEqualsValidator.class)
@Documented
public @interface FieldEquals {
	String message() default "{com.moekr.aes.util.validate.FieldEquals}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default { };

	String firstField();

	String secondField();

	@Target({TYPE, ANNOTATION_TYPE})
	@Retention(RUNTIME)
	@interface List {
		FieldEquals[] value();
	}

}
