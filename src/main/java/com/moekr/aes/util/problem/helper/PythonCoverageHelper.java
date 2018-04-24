package com.moekr.aes.util.problem.helper;

import com.moekr.aes.util.enums.FileType;

public class PythonCoverageHelper implements ProblemHelper {
	@Override
	public FileType fileType(String filePath) {
		if (filePath.startsWith("/test/")) {
			return FileType.PUBLIC;
		} else {
			return FileType.PROTECTED;
		}
	}

	@Override
	public String dockerFile(String uniqueName) {
		return "FROM ubuntu:16.04\n" +
				"RUN apt update && apt install --no-install-recommends -y python3-all python3-pip python3-nose\n" +
				"COPY ./code /var/ws/code\n" +
				"WORKDIR /var/ws/code/" + uniqueName + "\n" +
				"RUN pip3 install coverage && pip3 install -r requirements.txt || :\n";
	}

	@Override
	public String runScript(String uniqueName) {
		return "pushd /var/ws/code/" + uniqueName + "/\n"
				+ "nosetests3 --with-coverage --cover-xml || :\n"
				+ "popd\n"
				+ "mkdir -p ./coverage-reports/" + uniqueName + "/\n"
				+ "cp /var/ws/code/" + uniqueName + "/coverage.xml ./coverage-reports/" + uniqueName + "/ || :\n";
	}
}
