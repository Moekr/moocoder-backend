package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.CommitDAO;
import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.entity.*;
import com.moekr.moocoder.logic.api.JenkinsApi;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.enums.BuildStatus;
import com.moekr.moocoder.util.enums.ProblemType;
import com.moekr.moocoder.util.problem.evaluator.Evaluator;
import com.moekr.moocoder.util.problem.helper.ProblemHelper;
import com.offbytwo.jenkins.model.QueueItem;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Component
@CommonsLog
public class BuildManager {
	private final CommitDAO commitDAO;
	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;
	private final JenkinsApi jenkinsApi;
	private final ApplicationProperties properties;

	@Autowired
	public BuildManager(CommitDAO commitDAO, RecordDAO recordDAO, ResultDAO resultDAO, JenkinsApi jenkinsApi, ApplicationProperties properties) {
		this.commitDAO = commitDAO;
		this.recordDAO = recordDAO;
		this.resultDAO = resultDAO;
		this.jenkinsApi = jenkinsApi;
		this.properties = properties;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	@Transactional
	public boolean invokeNextBuild(int resultId) {
		Result result = resultDAO.findById(resultId);
		if (result == null) return false;
		Record record;
		while ((record = nextUnbuiltRecord(result)) != null) {
			try {
				QueueItem item = jenkinsApi.invokeBuild(resultId, buildParam(record));
				record.setNumber(item.getExecutable().getNumber().intValue());
				record.setStatus(BuildStatus.RUNNING);
				return true;
			} catch (Exception e) {
				log.error("触发构建" + record.getId() + "时发生异常" + ToolKit.format(e));
				record.setConsoleOutput("系统内部发生未知异常，请尝试重新提交！");
				record.setStatus(BuildStatus.FAILURE);
			} finally {
				recordDAO.save(record);
			}
		}
		return false;
	}

	@Transactional
	public boolean recordBuildResult(int id, int buildNumber) {
		Record record = recordDAO.findByResultIdAndBuildNumber(id, buildNumber);
		if (record == null || record.getStatus() != BuildStatus.RUNNING) {
			return false;
		}
		Problem problem = record.getProblem();
		ProblemType type = problem.getType();
		try {
			BuildDetails buildDetails = jenkinsApi.fetchBuildDetails(id, buildNumber, type.getTarget());
			Evaluator evaluator = type.getEvaluator();
			evaluator.evaluate(record, buildDetails);
		} catch (Exception e) {
			log.error("获取构建记录" + buildNumber + "时发生异常" + ToolKit.format(e));
			record.setConsoleOutput("系统内部发生未知异常，请尝试重新提交！");
			record.setStatus(BuildStatus.FAILURE);
		} finally {
			recordDAO.save(record);
		}
		return true;
	}

	private Record nextUnbuiltRecord(Result result) {
		Commit commit = commitDAO.findFirstUnfinishedByResult(result);
		if (commit == null) return null;
		Record unbuiltRecord = null;
		for (Record record : commit.getRecords()) {
			switch (record.getStatus()) {
				case RUNNING:
					return null;
				case WAITING:
					if (unbuiltRecord == null) {
						unbuiltRecord = record;
					}
			}
		}
		return unbuiltRecord;
	}

	private Map<String, String> buildParam(Record record) {
		Exam exam = record.getCommit().getResult().getExam();
		User user = record.getCommit().getResult().getOwner();
		Problem problem = record.getProblem();
		String uniqueName = problem.getUniqueName();
		ProblemHelper helper = problem.getType().getHelper();
		Map<String, String> param = new HashMap<>();
		param.put("PROB_TARGET", problem.getType().getTarget());
		param.put("GIT_URL", properties.getGitlab().getHost() + "/" + user.getUsername() + "/" + exam.getUuid());
		param.put("COMMIT_HASH", record.getCommit().getHash());
		param.put("DOCKER_IMAGE", properties.getDocker().getRegistry() + "/" + problem.getImageName() + ":" + problem.getImageTag());
		param.put("EXECUTE_SHELL", "#!/bin/bash\n" +
				"cp --parents -R " + uniqueName + helper.editableDirectory() + "/*" + " /var/ws/code/ &>/dev/null || :\n" +
				helper.runScript(uniqueName));
		return param;
	}
}
