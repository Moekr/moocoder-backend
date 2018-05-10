package com.moekr.moocoder.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public abstract class ToolKit {
	public static final String VERSION = "0.4.1";

	public static String format(Exception exception) {
		return "[" + exception.getClass().getName() + "]:" + exception.getMessage();
	}

	public static HttpStatus httpStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode == null) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		try {
			return HttpStatus.valueOf(statusCode);
		} catch (Exception e) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static String randomPassword() {
		return RandomStringUtils.randomAlphanumeric(12);
	}
}
