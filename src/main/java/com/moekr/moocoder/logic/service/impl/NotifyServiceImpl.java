package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.logic.service.NotifyService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@CommonsLog
public class NotifyServiceImpl implements NotifyService {
	private final BuildManager buildManager;
	private final CommitManager commitManager;

	@Autowired
	public NotifyServiceImpl(BuildManager buildManager, CommitManager commitManager) {
		this.buildManager = buildManager;
		this.commitManager = commitManager;
	}

	@Override
	@Async
	public void webHook(int id, String commitHash) {
		if (commitManager.initializeCommit(id, commitHash)) {
			buildManager.invokeNextBuild(id);
		}
	}

	@Override
	@Async
	public void callback(int id, int buildNumber) {
		if (buildManager.recordBuildResult(id, buildNumber)) {
			while (!buildManager.invokeNextBuild(id)) {
				if (!commitManager.finalizeCommit(id)) {
					break;
				}
			}
		}
	}
}
