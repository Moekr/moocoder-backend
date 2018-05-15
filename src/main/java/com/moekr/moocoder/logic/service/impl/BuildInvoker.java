package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.CommitDAO;
import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.entity.*;
import com.moekr.moocoder.logic.api.JenkinsApi;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.enums.BuildStatus;
import com.moekr.moocoder.util.problem.helper.ProblemHelper;
import com.offbytwo.jenkins.model.QueueItem;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class BuildInvoker {
	private final CommitDAO commitDAO;
	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;
	private final JenkinsApi jenkinsApi;
	private final ApplicationProperties properties;

	@Autowired
	public BuildInvoker(CommitDAO commitDAO, RecordDAO recordDAO, ResultDAO resultDAO, JenkinsApi jenkinsApi, ApplicationProperties properties) {
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
				log.error("触发构建#" + record.getId() + "时发生异常" + ToolKit.format(e));
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
		Map<Problem, Record> recordMap = commit.getRecords().stream()
				.collect(Collectors.toMap(Record::getProblem, r -> r));
		Result result = commit.getResult();
		Exam exam = result.getExam();
		Set<Problem> problems = exam.getProblems();
		int totalScore = 0;
		for (Problem problem : problems) {
			Record record = recordMap.get(problem);
			if (record == null) {
				record = recordDAO.findLastBuiltByResultAndProblem(result, problem);
			}
			if (record != null) {
				totalScore = totalScore + record.getScore();
			}
		}
		commit.setScore(totalScore / problems.size());
		commit.setFinished(true);
		commit = commitDAO.save(commit);
		if (commit.getScore() > result.getScore()) {
			result.setScore(commit.getScore());
			resultDAO.save(result);
		}
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
