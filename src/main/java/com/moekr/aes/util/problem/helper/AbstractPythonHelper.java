package com.moekr.aes.util.problem.helper;

import java.util.List;

public abstract class AbstractPythonHelper implements ProblemHelper {
	@Override
	public boolean validate(List<String> fileList) {
		if (fileList.stream().filter(s -> s.startsWith("/src/")).noneMatch(s -> s.endsWith(".py"))) return false;
		if (fileList.stream().filter(s -> s.startsWith("/test/")).noneMatch(s -> s.endsWith(".py"))) return false;
		return fileList.contains("/requirements.txt");
	}
}
