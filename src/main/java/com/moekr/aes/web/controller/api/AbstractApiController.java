package com.moekr.aes.web.controller.api;

import com.moekr.aes.util.editors.PageNumberEditor;
import com.moekr.aes.util.editors.RangeNumberEditor;
import com.moekr.aes.util.exceptions.InvalidRequestException;
import com.moekr.aes.util.exceptions.ServiceException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

public abstract class AbstractApiController {
	@InitBinder("page")
	public void initPageBinder(WebDataBinder binder) {
		binder.registerCustomEditor(int.class, new PageNumberEditor());
	}

	@InitBinder("limit")
	public void initLimitBinder(WebDataBinder binder) {
		binder.registerCustomEditor(int.class, new RangeNumberEditor(1, 100, 10));
	}

	protected void checkErrors(Errors errors) throws ServiceException {
		if (errors.hasErrors()) {
			throw new InvalidRequestException(errors.getGlobalError().getDefaultMessage());
		}
	}
}
