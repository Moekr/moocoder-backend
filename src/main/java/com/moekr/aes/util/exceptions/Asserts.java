package com.moekr.aes.util.exceptions;

public abstract class Asserts {
	public static void notNull(Object object, String message) throws EntityNotFoundException {
		if (object == null) {
			throw new EntityNotFoundException(message);
		}
	}

	public static void isNull(Object object, String message) throws EntityConflictException {
		if (object != null) {
			throw new EntityConflictException(message);
		}
	}
}
