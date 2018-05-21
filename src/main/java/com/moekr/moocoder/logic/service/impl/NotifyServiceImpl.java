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
		// 尝试创建一个新的提交记录
		if (commitManager.initializeCommit(id, commitHash)) {
			// 提交记录创建成功
			// 尝试触发第一次构建
			if (!buildManager.invokeNextBuild(id)) {
				// 触发构建失败，可能的原因：
				// 1、当前考生在当前考试中已经有构建正在进行
				// 2、新创建的提交记录中所有构建均触发失败
				// 立即关闭该提交
				commitManager.finalizeCommit(id);
			}
		}
	}

	@Override
	@Async
	public void callback(int id, int buildNumber) {
		// 尝试记录构建结果
		if (buildManager.recordBuildResult(id, buildNumber)) {
			// 记录构建结果成功
			// 尝试触发下一次构建
			while (!buildManager.invokeNextBuild(id)) {
				// 触发构建失败，可能的原因：
				// 1、当前运行的提交中没有可以运行的构建
				// 2、当前运行的提交中所有剩余构建均触发失败
				// 3、当前考生在当前考试中已经有构建正在进行
				// 尝试关闭当前运行的提交
				// 成功关闭当前运行的提交则继续尝试触发下一次构建
				if (!commitManager.finalizeCommit(id)) {
					break;
				}
			}
		}
	}
}
