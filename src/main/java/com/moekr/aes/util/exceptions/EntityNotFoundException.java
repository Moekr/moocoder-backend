package com.moekr.aes.util.exceptions;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.NOT_FOUND;

	public EntityNotFoundException(String message) {
		super(HTTP_STATUS.value(), message);
	}
}
