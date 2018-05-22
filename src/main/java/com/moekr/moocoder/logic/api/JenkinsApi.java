package com.moekr.moocoder.logic.api;

import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.offbytwo.jenkins.model.QueueItem;

import java.io.IOException;
import java.util.Map;

public interface JenkinsApi {
	void createJob(int id) throws IOException;

	QueueItem invokeBuild(int id, Map<String, String> paramMap) throws IOException;

	void deleteJob(int id) throws IOException;

	// waitFinished: 是否等待直到构建完全完成
	BuildDetails fetchBuildDetails(int id, int buildNumber, String target, boolean waitFinished) throws IOException;
}
