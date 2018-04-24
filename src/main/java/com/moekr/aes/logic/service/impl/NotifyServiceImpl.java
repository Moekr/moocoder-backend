package com.moekr.aes.logic.service.impl;

import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.logic.service.NotifyService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommonsLog
public class NotifyServiceImpl implements NotifyService {
	private final JenkinsApi jenkinsApi;
	private final BuildInvoker buildInvoker;
	private final BuildReportRecorder buildReportRecorder;
	private final CommitInitializer commitInitializer;

	@Autowired
	public NotifyServiceImpl(JenkinsApi jenkinsApi, BuildInvoker buildInvoker, BuildReportRecorder buildReportRecorder, CommitInitializer commitInitializer) {
		this.jenkinsApi = jenkinsApi;
		this.buildInvoker = buildInvoker;
		this.buildReportRecorder = buildReportRecorder;
		this.commitInitializer = commitInitializer;
	}

	@Override
	public void webHook(int id, String commitHash) {
		commitInitializer.initializeCommit(id, commitHash);
		buildInvoker.invokeNextBuild(id);
	}

	@Override
	public void callback(int id, int buildNumber) {
		BuildDetails buildDetails = null;
		try {
			buildDetails = jenkinsApi.fetchBuildDetails(id, buildNumber);
		} catch (Exception e) {
			log.error("获取项目#" + id + "的构建记录#" + buildNumber + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
		}
		if (buildDetails != null) {
			buildReportRecorder.record(id, buildDetails);
		}
		buildInvoker.invokeNextBuild(id);
	}
}
