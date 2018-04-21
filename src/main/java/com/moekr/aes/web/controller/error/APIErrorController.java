package com.moekr.aes.web.controller.error;

import com.moekr.aes.util.ToolKit;
import com.moekr.aes.web.response.ErrorResponse;
import com.moekr.aes.web.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(GlobalErrorController.ERROR_PATH)
public class APIErrorController {
	@RequestMapping
	public Response error(HttpServletRequest request) {
		HttpStatus httpStatus = ToolKit.httpStatus(request);
		return new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase());
	}
}
