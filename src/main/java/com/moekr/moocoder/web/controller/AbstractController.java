package com.moekr.moocoder.web.controller;

import com.moekr.moocoder.util.exceptions.InvalidRequestException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import org.springframework.validation.Errors;

public abstract class AbstractController {
	protected void checkErrors(Errors errors) throws ServiceException {
		if (errors.hasGlobalErrors()) {
			throw new InvalidRequestException(errors.getGlobalError().getDefaultMessage());
		}
		if (errors.hasFieldErrors()) {
			throw new InvalidRequestException(errors.getFieldError().getDefaultMessage());
		}
	}
}
