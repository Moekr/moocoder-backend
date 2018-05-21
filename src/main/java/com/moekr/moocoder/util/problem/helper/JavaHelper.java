package com.moekr.moocoder.util.problem.helper;

import com.moekr.moocoder.util.enums.FileType;

public class JavaHelper extends AbstractJavaHelper {
	@Override
	public String editableDirectory() {
		return "/src/main";
	}

	@Override
	public FileType fileType(String filePath) {
		if (filePath.startsWith("/src/main/")) {
			return FileType.PUBLIC;
		} else if (filePath.startsWith("/src/test/")) {
			return FileType.PRIVATE;
		} else {
			return FileType.PROTECTED;
		}
	}

	@Override
	public String dockerFile(String uniqueName) {
		return "FROM ubuntu:16.04\n" +
				"RUN apt update && apt install --no-install-recommends -y default-jre default-jdk maven\n" +
				"COPY ./code /var/ws/code\n" +
				"WORKDIR /var/ws/code/" + uniqueName + "\n" +
				"RUN mvn -fn clean test clean -Dmaven.repo.local=/var/ws/repository/ || :\n";
	}

	@Override
	public String runScript(String uniqueName) {
		return "pushd /var/ws/tmp/ &>/dev/null\n"
				+ "mvn -fn clean test -Dmaven.repo.local=/var/ws/repository/\n"
				+ "popd &>/dev/null\n"
				+ "rm -rf test-reports &>/dev/null\n"
				+ "mkdir test-reports &>/dev/null\n"
				+ "cp /var/ws/tmp/target/surefire-reports/*.xml ./test-reports/ &>/dev/null || :\n";
	}
}
