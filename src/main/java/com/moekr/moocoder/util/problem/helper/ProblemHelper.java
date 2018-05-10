package com.moekr.moocoder.util.problem.helper;

import com.moekr.moocoder.util.enums.FileType;

import java.util.List;

public interface ProblemHelper {
	boolean validate(List<String> fileList);

	String editableDirectory();

	FileType fileType(String filePath);

	String dockerFile(String uniqueName);

	String runScript(String uniqueName);
}
