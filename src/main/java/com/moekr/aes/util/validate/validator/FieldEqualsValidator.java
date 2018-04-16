package com.moekr.aes.util.validate.validator;

import com.moekr.aes.util.validate.FieldEquals;
import org.apache.commons.beanutils.BeanUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class FieldEqualsValidator implements ConstraintValidator<FieldEquals, Object> {
	private String firstField;
	private String secondField;

	@Override
	public void initialize(FieldEquals constraintAnnotation) {
		this.firstField = constraintAnnotation.firstField();
		this.secondField = constraintAnnotation.secondField();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		try {
			Object firstObject = BeanUtils.getProperty(value, firstField);
			Object secondObject = BeanUtils.getProperty(value, secondField);
			return Objects.equals(firstObject, secondObject);
		} catch (Exception e) {
			return false;
		}
	}
}
