package com.moekr.moocoder.util.validate;

import com.moekr.moocoder.util.validate.FieldCompare.List;
import com.moekr.moocoder.util.validate.validator.FieldCompareValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Repeatable(List.class)
@Constraint(validatedBy = FieldCompareValidator.class)
@Documented
public @interface FieldCompare {
	String message() default "{com.moekr.moocoder.util.validate.FieldCompare}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default { };

	String lessField();

	String greaterField();

	boolean allowEqual() default false;

	@Target({TYPE, ANNOTATION_TYPE})
	@Retention(RUNTIME)
	@Documented
	@interface List {
		FieldCompare[] value();
	}
}
