package com.moekr.aes.util.exceptions;

import org.springframework.http.HttpStatus;

public class ConflictException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;

	public ConflictException() {
		this(HTTP_STATUS.getReasonPhrase());
	}

	public ConflictException(String message) {
		super(HTTP_STATUS.value(), message);
	}
}
