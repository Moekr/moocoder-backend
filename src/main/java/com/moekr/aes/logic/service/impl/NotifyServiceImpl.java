package com.moekr.aes.logic.service.impl;

import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.logic.service.NotifyService;
import com.offbytwo.jenkins.model.QueueItem;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@CommonsLog
public class NotifyServiceImpl implements NotifyService {
	private final JenkinsApi jenkinsApi;
	private final BuildReportRecorder recorder;

	public NotifyServiceImpl(JenkinsApi jenkinsApi, BuildReportRecorder recorder) {
		this.jenkinsApi = jenkinsApi;
		this.recorder = recorder;
	}

	@Override
	@Async
	public void webHook(int id) {
		QueueItem item = null;
		try {
			item = jenkinsApi.invokeBuild(id);
		} catch (Exception e) {
			log.error("触发项目#" + id + "构建时发生异常[" + e.getClass() + "]: " + e.getMessage());
		}
		if (item != null) {
			recorder.record(id, item.getExecutable().getNumber().intValue());
		}
	}

	@Override
	@Async
	public void callback(int id, int buildNumber) {
		BuildDetails buildDetails = null;
		try {
			buildDetails = jenkinsApi.fetchBuildDetails(id, buildNumber);
		} catch (Exception e) {
			log.error("获取项目#" + id + "的构建记录#" + buildNumber + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
		}
		if (buildDetails != null) {
			recorder.record(id, buildDetails);
		}
	}
}
