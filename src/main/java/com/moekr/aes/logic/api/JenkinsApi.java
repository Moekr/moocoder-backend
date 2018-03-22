package com.moekr.aes.logic.api;

import com.moekr.aes.util.enums.Language;
import com.moekr.aes.util.enums.Role;
import com.offbytwo.jenkins.model.TestResult;

public interface JenkinsApi {
	String createJob(int id, String namespace, String project, String problem, Language language, Role role);

	void deleteJob(String name);

	TestResult fetchTestResult(int id, int buildNumber);
}
