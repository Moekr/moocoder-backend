package com.moekr.aes.util.enums;

public enum Language {
	JAVA("Java"), PYTHON("Python");

	private final String name;

	Language(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
