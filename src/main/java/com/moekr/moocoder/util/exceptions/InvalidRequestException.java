package com.moekr.moocoder.util.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

	public InvalidRequestException() {
		this(HTTP_STATUS.getReasonPhrase());
	}

	public InvalidRequestException(String message) {
		super(HTTP_STATUS.value(), message);
	}
}
