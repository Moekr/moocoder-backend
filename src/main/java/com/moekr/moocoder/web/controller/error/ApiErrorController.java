package com.moekr.moocoder.web.controller.error;

import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.web.response.ErrorResponse;
import com.moekr.moocoder.web.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(GlobalErrorController.ERROR_PATH)
public class ApiErrorController {
	@RequestMapping
	public Response error(HttpServletRequest request) {
		HttpStatus httpStatus = ToolKit.httpStatus(request);
		return new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase());
	}
}
