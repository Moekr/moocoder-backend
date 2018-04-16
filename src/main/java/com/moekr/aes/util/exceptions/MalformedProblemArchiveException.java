package com.moekr.aes.util.exceptions;

import org.springframework.http.HttpStatus;

public class MalformedProblemArchiveException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;

	public MalformedProblemArchiveException(String message) {
		super(HTTP_STATUS.value(), message);
	}
}
