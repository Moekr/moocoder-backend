package com.moekr.aes.util.enums;

public enum Role {
	STUDENT("学生"), TEACHER("教师");

	private final String name;

	Role(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
