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
		List<Commit> commitList = commitDAO.findAllByResult_IdAndFinishedOrderByIdAsc(resultId, false);
		for (Commit commit : commitList) {
			Set<Record> records = commit.getRecords();
			Record record = records.stream()
					.filter(r -> r.getStatus() == BuildStatus.RUNNING)
					.findFirst().orElse(null);
			if (record == null) {
				record = records.stream()
						.filter(r -> r.getStatus() == BuildStatus.WAITING)
						.findFirst().orElse(null);
				if (record != null) {
					try {
						QueueItem item = jenkinsApi.invokeBuild(resultId, buildParam(record));
						record.setNumber(item.getExecutable().getNumber().intValue());
						record.setStatus(BuildStatus.RUNNING);
					} catch (Exception e) {
						log.error("触发#" + resultId + "/" + commit.getId() + "/" + record.getId() + "构建时发生异常");
						record.setStatus(BuildStatus.FAILURE);
					}
					recordDAO.save(record);
					break;
				} else {
					commit.setScore(records.stream()
							.map(Record::getScore)
							.reduce((a, b) -> a + b)
							.orElse(0) / records.size());
					commit.setFinished(true);
					commitDAO.save(commit);
					Result result = commit.getResult();
					if (commit.getScore() > result.getScore()) {
						result.setScore(commit.getScore());
						resultDAO.save(result);
					}
				}
			} else {
				break;
			}
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
			builder.append("cp --parents ").append(problem.getName()).append(publicFile).append(" /var/ws/code/ || :\n");
		}
		builder.append(problem.getType().runScript(problem.getName()));
		param.put("EXECUTE_SHELL", builder.toString());
		return param;
	}
}
