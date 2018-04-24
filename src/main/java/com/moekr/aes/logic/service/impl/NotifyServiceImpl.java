package com.moekr.aes.logic.service.impl;

import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.logic.service.NotifyService;
import com.moekr.aes.util.ToolKit;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@CommonsLog
public class NotifyServiceImpl implements NotifyService {
	private final JenkinsApi jenkinsApi;
	private final BuildInvoker buildInvoker;
	private final BuildRecorder buildRecorder;
	private final CommitInitializer commitInitializer;

	@Autowired
	public NotifyServiceImpl(JenkinsApi jenkinsApi, BuildInvoker buildInvoker, BuildRecorder buildRecorder, CommitInitializer commitInitializer) {
		this.jenkinsApi = jenkinsApi;
		this.buildInvoker = buildInvoker;
		this.buildRecorder = buildRecorder;
		this.commitInitializer = commitInitializer;
	}

	@Override
	@Async
	public void webHook(int id, String commitHash) {
		if (commitInitializer.initializeCommit(id, commitHash)) {
			buildInvoker.invokeNextBuild(id);
		}
	}

	@Override
	@Async
	public void callback(int id, int buildNumber) {
		BuildDetails buildDetails = null;
		try {
			buildDetails = jenkinsApi.fetchBuildDetails(id, buildNumber);
		} catch (Exception e) {
			log.error("获取项目" + id + "的构建记录" + buildNumber + "时发生异常" + ToolKit.format(e));
		}
		buildRecorder.record(id, buildNumber, buildDetails);
		buildInvoker.invokeNextBuild(id);
	}
}
