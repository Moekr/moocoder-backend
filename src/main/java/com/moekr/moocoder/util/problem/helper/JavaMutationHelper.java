package com.moekr.moocoder.util.problem.helper;

public class JavaMutationHelper extends JavaCoverageHelper {
	@Override
	public String dockerFile(String uniqueName) {
		return "FROM ubuntu:16.04\n" +
				"RUN apt update && apt install --no-install-recommends -y default-jre default-jdk maven\n" +
				"COPY ./code /var/ws/code\n" +
				"WORKDIR /var/ws/code/" + uniqueName + "\n" +
				"RUN mvn -fn clean compile org.pitest:pitest-maven:mutationCoverage clean -Dmaven.repo.local=/var/ws/repository/ || :\n";
	}

	@Override
	public String runScript(String uniqueName) {
		return "pushd /var/ws/tmp/ &>/dev/null\n"
				+ "mvn -fn clean compile org.pitest:pitest-maven:mutationCoverage -DoutputFormats=xml -Dmaven.repo.local=/var/ws/repository/\n"
				+ "popd &>/dev/null\n"
				+ "rm -rf mutation-reports &>/dev/null\n"
				+ "mkdir mutation-reports &>/dev/null\n"
				+ "cp /var/ws/tmp/target/pit-reports/*/mutations.xml ./mutation-reports/ &>/dev/null || :\n";
	}
}
