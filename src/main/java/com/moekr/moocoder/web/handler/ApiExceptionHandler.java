package com.moekr.moocoder.web.handler;

import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.response.ErrorResponse;
import com.moekr.moocoder.web.response.Response;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice({"com.moekr.moocoder.web.controller.api", "com.moekr.moocoder.web.controller.internal"})
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
