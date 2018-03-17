package com.moekr.aes.logic.api;

import com.moekr.aes.util.enums.Language;
import com.offbytwo.jenkins.model.TestReport;
import com.offbytwo.jenkins.model.TestResult;

public interface JenkinsApi {
	String createJob(int id, String namespace, String project, String problem, Language language);

	void deleteJob(String name);

	//For Maven Project
	TestReport fetchTestReport(int id, int buildNumber);

	//For Non-Maven Project
	TestResult fetchTestResult(int id, int buildNumber);
}
