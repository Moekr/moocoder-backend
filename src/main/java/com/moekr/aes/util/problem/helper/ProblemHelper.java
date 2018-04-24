package com.moekr.aes.util.problem.helper;

import com.moekr.aes.util.enums.FileType;

public interface ProblemHelper {
	FileType fileType(String filePath);

	String dockerFile(String uniqueName);

	String runScript(String uniqueName);
}
