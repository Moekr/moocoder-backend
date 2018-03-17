package com.moekr.aes.web.handler;

import com.moekr.aes.util.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice("com.moekr.aes.web.controller.view")
public class ViewExceptionHandler {
	@ExceptionHandler(Exception.class)
	public String handle(HttpServletResponse response, Model model, Exception exception) {
		int error = HttpStatus.SC_INTERNAL_SERVER_ERROR;
		if (exception instanceof ServiceException) {
			error = ((ServiceException) exception).getError();
		}
		response.setStatus(error);
		model.addAttribute("error", error);
		model.addAttribute("message", StringUtils.abbreviate(exception.getMessage(), 80));
		exception.printStackTrace();
		return "error";
	}
}
