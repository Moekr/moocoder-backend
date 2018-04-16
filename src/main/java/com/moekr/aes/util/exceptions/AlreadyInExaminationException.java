package com.moekr.aes.util.exceptions;

import org.springframework.http.HttpStatus;

public class AlreadyInExaminationException extends ServiceException {
	private static final HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;
	private static final String MESSAGE = "已经参加了所选考试！";

	public AlreadyInExaminationException() {
		super(HTTP_STATUS.value(), MESSAGE);
	}
}
