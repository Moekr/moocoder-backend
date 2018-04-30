package com.moekr.aes.web.controller.api;

import com.moekr.aes.util.editors.DefaultNumberEditor;
import com.moekr.aes.util.editors.PageNumberEditor;
import com.moekr.aes.util.editors.RangeNumberEditor;
import com.moekr.aes.util.exceptions.InvalidRequestException;
import com.moekr.aes.util.exceptions.ServiceException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

public abstract class AbstractApiController {
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		switch (binder.getObjectName()) {
			case "page":
				binder.registerCustomEditor(int.class, new PageNumberEditor());
				return;
			case "limit":
				binder.registerCustomEditor(int.class, new RangeNumberEditor(1, 100, 10));
				return;
		}
		binder.registerCustomEditor(int.class, new DefaultNumberEditor(-1));
	}

	protected void checkErrors(Errors errors) throws ServiceException {
		if (errors.hasGlobalErrors()) {
			throw new InvalidRequestException(errors.getGlobalError().getDefaultMessage());
		}
		if (errors.hasFieldErrors()) {
			throw new InvalidRequestException(errors.getFieldError().getDefaultMessage());
		}
	}
}
