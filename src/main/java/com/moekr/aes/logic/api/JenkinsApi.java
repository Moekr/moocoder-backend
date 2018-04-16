package com.moekr.aes.logic.api;

import com.moekr.aes.logic.api.vo.BuildDetails;
import com.offbytwo.jenkins.model.QueueItem;

import java.io.IOException;

public interface JenkinsApi {
	void createJob(int id) throws IOException;

	QueueItem invokeBuild(int id) throws IOException;

	void deleteJob(int id) throws IOException;

	BuildDetails fetchBuildDetails(int id, int buildNumber) throws IOException;
}
