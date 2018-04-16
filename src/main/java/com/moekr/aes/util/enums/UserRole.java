package com.moekr.aes.util.enums;

public enum UserRole {
	STUDENT("学生"), TEACHER("教师");

	private final String name;

	UserRole(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
