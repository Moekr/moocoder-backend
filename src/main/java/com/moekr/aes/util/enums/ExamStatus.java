package com.moekr.aes.util.enums;

/**
 * 考试状态
 * 考试生命周期中对外表现的状态的顺序为PREPARING->READY->AVAILABLE->FINISHED->CLOSED
 * 考试生命周期中持久化的状态的顺序为PREPARING->AVAILABLE->CLOSED
 * UNAVAILABLE表示考试初始化失败
 */
public enum ExamStatus {
	/**
	 * 正在准备试题，通常是正在PUSH试题
	 */
	PREPARING,
	/**
	 * 考试不可用，通常是PUSH试题失败
	 */
	UNAVAILABLE,
	/**
	 * 考试已准备好，在考试尚未开始时使用，不持久化到数据库中
	 */
	READY,
	/**
	 * 考试可用，表示考试正在进行，持久化时考试从准备好到考试被关闭均为此状态
	 */
	AVAILABLE,
	/**
	 * 考试已完成，在考试结束后使用，不持久化到数据库中
	 */
	FINISHED,
	/**
	 * 考试已关闭，表示考试生命周期结束，所有试卷均被存档
	 */
	CLOSED
}
