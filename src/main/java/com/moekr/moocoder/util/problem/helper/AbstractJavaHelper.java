package com.moekr.moocoder.util.problem.helper;

import java.util.List;

public abstract class AbstractJavaHelper implements ProblemHelper {
	@Override
	public boolean validate(List<String> fileList) {
		if (fileList.stream().filter(s -> s.startsWith("/src/main/java/")).noneMatch(s -> s.endsWith(".java"))) return false;
		if (fileList.stream().filter(s -> s.startsWith("/src/test/java/")).noneMatch(s -> s.endsWith(".java"))) return false;
		return fileList.contains("/pom.xml");
	}
}
