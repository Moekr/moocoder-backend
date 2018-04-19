package com.moekr.aes.web.handler;

import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.util.ToolKit;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestControllerAdvice("com.moekr.aes.web.controller.api")
public class APIExceptionHandler {
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public Map<String, Object> handle(HttpServletResponse response, Exception exception) {
		int error = HttpStatus.SC_INTERNAL_SERVER_ERROR;
		if (exception instanceof ServiceException) {
			error = ((ServiceException) exception).getError();
		}
		response.setStatus(error);
		exception.printStackTrace();
		return ToolKit.assemblyResponseBody(error, exception.getMessage());
	}
}
