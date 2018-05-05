package com.moekr.moocoder.util.enums;

public enum BuildStatus {
	/**
	 * 构建成功，表示没有错误
	 */
	SUCCESS,
	/**
	 * 构建不稳定，表示有测试错误等非致命错误
	 */
	UNSTABLE,
	/**
	 * 构建失败，表示有语法错误等致命错误
	 */
	FAILURE,
	/**
	 * 构建超时，表示运行测试时超时
	 */
	TIMEOUT,
	/**
	 * 等待构建，表示构建尚未开始
	 */
	WAITING,
	/**
	 * 正在构建，表示构建正在进行
	 */
	RUNNING
}
