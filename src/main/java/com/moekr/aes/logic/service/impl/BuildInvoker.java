package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.CommitDAO;
import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.*;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.enums.BuildStatus;
import com.offbytwo.jenkins.model.QueueItem;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@CommonsLog
public class BuildInvoker {
	private final CommitDAO commitDAO;
	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;
	private final JenkinsApi jenkinsApi;
	private final AesProperties properties;

	@Autowired
	public BuildInvoker(CommitDAO commitDAO, RecordDAO recordDAO, ResultDAO resultDAO, JenkinsApi jenkinsApi, AesProperties properties) {
		this.commitDAO = commitDAO;
		this.recordDAO = recordDAO;
		this.resultDAO = resultDAO;
		this.jenkinsApi = jenkinsApi;
		this.properties = properties;
	}

	@Transactional
	public void invokeNextBuild(int resultId) {
		Record record;
		while ((record = nextUnbuiltRecord(resultId)) != null) {
			try {
				QueueItem item = jenkinsApi.invokeBuild(resultId, buildParam(record));
				record.setNumber(item.getExecutable().getNumber().intValue());
				record.setStatus(BuildStatus.RUNNING);
				break;
			} catch (Exception e) {
				log.error("触发构建#" + record.getId() + "时发生异常[" + e.getClass() + "]:" + e.getMessage());
				record.setStatus(BuildStatus.FAILURE);
			} finally {
				recordDAO.save(record);
			}
		}

	}

	private Record nextUnbuiltRecord(int resultId) {
		List<Commit> commitList = commitDAO.findAllByResult_IdAndFinishedOrderByIdAsc(resultId, false);
		for (Commit commit : commitList) {
			for (Record record : commit.getRecords()) {
				if (record.getStatus() == BuildStatus.RUNNING) {
					return null;
				} else if (record.getStatus() == BuildStatus.WAITING) {
					return record;
				}
			}
			completeCommit(commit);
		}
		return null;
	}

	private void completeCommit(Commit commit) {
		Set<Record> records = commit.getRecords();
		commit.setScore(records.stream()
				.map(Record::getScore)
				.reduce((a, b) -> a + b)
				.orElse(0) / Math.max(records.size(), 1));
		commit.setFinished(true);
		commit = commitDAO.save(commit);
		Result result = commit.getResult();
		if (commit.getScore() > result.getScore()) {
			result.setScore(commit.getScore());
			resultDAO.save(result);
		}
	}

	private Map<String, String> buildParam(Record record) {
		Exam exam = record.getCommit().getResult().getExam();
		User user = record.getCommit().getResult().getOwner();
		Problem problem = record.getProblem();
		Map<String, String> param = new HashMap<>();
		param.put("GIT_URL", properties.getGitlab().getHost() + "/" + user.getUsername() + "/" + exam.getUuid());
		param.put("COMMIT_HASH", record.getCommit().getHash());
		param.put("DOCKER_IMAGE", properties.getDocker().getRegistry() + "/" + problem.getImageName() + ":" + problem.getImageTag());
		StringBuilder builder = new StringBuilder();
		builder.append("#!/bin/bash\n");
		for (String publicFile : problem.getPublicFiles()) {
			builder.append("cp --parents ").append(problem.getUniqueName()).append(publicFile).append(" /var/ws/code/ &>/dev/null || :\n");
		}
		builder.append(problem.getType().getHelper().runScript(problem.getUniqueName()));
		param.put("EXECUTE_SHELL", builder.toString());
		return param;
	}
}
