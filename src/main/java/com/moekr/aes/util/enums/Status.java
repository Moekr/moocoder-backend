package com.moekr.aes.util.enums;

public enum Status {
	BEFORE("尚未开始"), ONGOING("正在进行"), AFTER("已经结束");

	private final String name;

	Status(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
