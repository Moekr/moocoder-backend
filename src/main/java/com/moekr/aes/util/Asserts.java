package com.moekr.aes.util;

public abstract class Asserts {
	public static void isTrue(boolean condition) {
		if (!condition) {
			throw new ServiceException();
		}
	}

	public static void isTrue(boolean condition, int error) {
		if (!condition) {
			throw new ServiceException(error);
		}
	}

	public static void isTrue(boolean condition, String message) {
		if (!condition) {
			throw new ServiceException(message);
		}
	}

	public static void isTrue(boolean condition, int error, String message) {
		if (!condition) {
			throw new ServiceException(error, message);
		}
	}
}
