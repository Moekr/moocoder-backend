package com.moekr.aes.util.exceptions;

import org.springframework.http.HttpStatus;

public class EntityNotAvailableException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.UNPROCESSABLE_ENTITY;

	public EntityNotAvailableException(String message) {
		super(HTTP_STATUS.value(), message);
	}
}
