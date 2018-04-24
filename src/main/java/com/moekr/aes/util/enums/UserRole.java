package com.moekr.aes.util.enums;

public enum UserRole {
	/**
	 * 学生角色
	 */
	STUDENT("学生"),
	/**
	 * 教师角色
	 */
	TEACHER("教师");

	private final String name;

	UserRole(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
