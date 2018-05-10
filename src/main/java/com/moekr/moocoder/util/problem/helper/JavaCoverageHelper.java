package com.moekr.moocoder.util.problem.helper;

import com.moekr.moocoder.util.enums.FileType;

public class JavaCoverageHelper extends AbstractJavaHelper {
	@Override
	public String editableDirectory() {
		return "/src/test";
	}

	@Override
	public FileType fileType(String filePath) {
		if (filePath.startsWith("/src/test/")) {
			return FileType.PUBLIC;
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
				"RUN mvn -fn clean cobertura:cobertura clean -Dmaven.repo.local=/var/ws/repository/ || :\n";
	}

	@Override
	public String runScript(String uniqueName) {
		return "pushd /var/ws/code/" + uniqueName + "/ &>/dev/null\n"
				+ "mvn -fn clean cobertura:cobertura -Dcobertura.report.format=xml -Dmaven.repo.local=/var/ws/repository/\n"
				+ "popd &>/dev/null\n"
				+ "mkdir -p ./coverage-reports/" + uniqueName + "/ &>/dev/null\n"
				+ "cp /var/ws/code/" + uniqueName + "/target/site/cobertura/*.xml ./coverage-reports/" + uniqueName + "/ &>/dev/null || :\n";
	}
}
