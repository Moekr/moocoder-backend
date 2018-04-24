package com.moekr.aes.util.enums;

public enum FileType {
	/**
	 * 可修改的文件，表示学生可以修改该文件，进行测试时应用学生的文件替换试题中的文件
	 */
	PUBLIC,
	/**
	 * 不可修改的文件，表示学生不可以修改该文件，进行测试时保持试题文件不变化
	 */
	PROTECTED,
	/**
	 * 隐藏文件，表示学生无法看到该文件，通常用于测试用例等
	 */
	PRIVATE
}
