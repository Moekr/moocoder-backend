package com.moekr.aes.util.exceptions;

public class UnsupportedProblemTypeException extends MalformedProblemArchiveException {
	public UnsupportedProblemTypeException(String type) {
		super("不支持的题目类型[" + type + "]");
	}
}
