package com.moekr.aes.web.handler;

import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.response.ErrorResponse;
import com.moekr.aes.web.response.Response;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice({"com.moekr.aes.web.controller.api", "com.moekr.aes.web.controller.internal"})
public class ApiExceptionHandler {
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Response handle(HttpServletResponse response, Exception exception) {
		int error = HttpStatus.SC_INTERNAL_SERVER_ERROR;
		if (exception instanceof ServiceException) {
			error = ((ServiceException) exception).getError();
		}
		response.setStatus(error);
		return new ErrorResponse(error, exception.getMessage());
	}
}
