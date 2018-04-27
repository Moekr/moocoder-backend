package com.moekr.aes.util.problem.helper;

import com.moekr.aes.util.enums.FileType;

public class PythonHelper extends AbstractPythonHelper {
	@Override
	public FileType fileType(String filePath) {
		if (filePath.startsWith("/src/")) {
			return FileType.PUBLIC;
		} else if (filePath.startsWith("/test/")) {
			return FileType.PRIVATE;
		} else  {
			return FileType.PROTECTED;
		}
	}

	@Override
	public String dockerFile(String uniqueName) {
		return "FROM ubuntu:16.04\n" +
				"RUN apt update && apt install --no-install-recommends -y python3-all python3-pip python3-nose\n" +
				"COPY ./code /var/ws/code\n" +
				"WORKDIR /var/ws/code/" + uniqueName + "\n" +
				"RUN pip3 install -r requirements.txt || :\n";
	}

	@Override
	public String runScript(String uniqueName) {
		return "pushd /var/ws/code/" + uniqueName + "/ &>/dev/null\n"
				+ "nosetests3 --with-xunit || :\n"
				+ "popd &>/dev/null\n"
				+ "mkdir -p ./test-reports/" + uniqueName + "/ &>/dev/null\n"
				+ "cp /var/ws/code/" + uniqueName + "/nosetests.xml ./test-reports/" + uniqueName + "/ &>/dev/null || :\n";
	}
}
