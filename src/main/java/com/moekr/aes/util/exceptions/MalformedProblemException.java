package com.moekr.aes.util.exceptions;

import org.springframework.http.HttpStatus;

public class MalformedProblemException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

	public MalformedProblemException(String message) {
		super(HTTP_STATUS.value(), message);
	}
}
