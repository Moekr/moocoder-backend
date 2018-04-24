package com.moekr.aes.util.exceptions;

public class AlreadyInExaminationException extends ConflictException {
	private static final String MESSAGE = "已经参加了所选考试！";

	public AlreadyInExaminationException() {
		super(MESSAGE);
	}
}
