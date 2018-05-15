package com.moekr.moocoder.util.problem.helper;

import com.moekr.moocoder.util.enums.FileType;

public class PythonCoverageHelper extends AbstractPythonHelper {
	@Override
	public String editableDirectory() {
		return "/test";
	}

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
		return "pushd /var/ws/code/" + uniqueName + "/ &>/dev/null\n"
				+ "nosetests3 --with-coverage --cover-xml || :\n"
				+ "popd &>/dev/null\n"
				+ "mkdir -p ./coverage-reports/" + uniqueName + "/ &>/dev/null\n"
				+ "cp /var/ws/code/" + uniqueName + "/coverage.xml ./coverage-reports/ &>/dev/null || :\n";
	}
}
