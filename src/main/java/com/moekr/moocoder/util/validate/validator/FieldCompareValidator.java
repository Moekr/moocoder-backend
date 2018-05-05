package com.moekr.moocoder.util.validate.validator;

import com.moekr.moocoder.util.validate.FieldCompare;
import org.springframework.beans.BeanUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FieldCompareValidator implements ConstraintValidator<FieldCompare, Object> {
	private String lessField;
	private String greaterField;
	private boolean allowEqual;


	@Override
	public void initialize(FieldCompare constraintAnnotation) {
		this.lessField = constraintAnnotation.lessField();
		this.greaterField = constraintAnnotation.greaterField();
		this.allowEqual = constraintAnnotation.allowEqual();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		try {
			Comparable lessObject = (Comparable) BeanUtils.getPropertyDescriptor(value.getClass(), lessField).getReadMethod().invoke(value);
			Comparable greaterObject = (Comparable) BeanUtils.getPropertyDescriptor(value.getClass(), greaterField).getReadMethod().invoke(value);
			int result = lessObject.compareTo(greaterObject);
			if (allowEqual && result == 0) {
				return true;
			} else {
				return result < 0;
			}
		} catch (Exception e) {
			return false;
		}
	}
}
