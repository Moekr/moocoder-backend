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
		return "pushd /var/ws/code/" + uniqueName + "/ &>/dev/null\n"
				+ "mvn -fn clean compile org.pitest:pitest-maven:mutationCoverage -DoutputFormats=xml -Dmaven.repo.local=/var/ws/repository/\n"
				// 1.0.16版本的PIT Mutation Plugin与最新的pitest-maven生成的xml报告不兼容，需要删除报告中的部分字段
				+ "find target/pit-reports -name mutations.xml | xargs sed -i -r -e \"s/numberOfTestsRun='[0-9]+'//g\" -e \"s/<block>[0-9]+<\\\\/block>//g\"\n"
				+ "popd &>/dev/null\n"
				+ "mkdir -p ./mutation-reports/" + uniqueName + "/ &>/dev/null\n"
				+ "cp /var/ws/code/" + uniqueName + "/target/pit-reports/*/mutations.xml ./mutation-reports/ &>/dev/null || :\n";
	}
}
