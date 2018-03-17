package com.moekr.aes.util;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class ServiceException extends RuntimeException {
	private int error;

	public ServiceException() {
		this(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
	}

	public ServiceException(int error) {
		this(error, HttpStatus.valueOf(error).getReasonPhrase());
	}

	public ServiceException(String message) {
		this(HttpStatus.INTERNAL_SERVER_ERROR.value(), message);
	}

	public ServiceException(int error, String message) {
		super(message);
		this.error = error;
	}
}
