package com.moekr.aes.util.problem.helper;

import com.moekr.aes.util.enums.FileType;

import java.util.List;

public interface ProblemHelper {
	boolean validate(List<String> fileList);

	FileType fileType(String filePath);

	String dockerFile(String uniqueName);

	String runScript(String uniqueName);
}
