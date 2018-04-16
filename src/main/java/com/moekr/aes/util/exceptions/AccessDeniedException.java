package com.moekr.aes.util.exceptions;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.FORBIDDEN;

	public AccessDeniedException() {
		this(HTTP_STATUS.getReasonPhrase());
	}

	public AccessDeniedException(String message) {
		super(HTTP_STATUS.value(), message);
	}
}
