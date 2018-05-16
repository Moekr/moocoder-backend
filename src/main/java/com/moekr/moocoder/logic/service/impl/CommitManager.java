package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.CommitDAO;
import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.entity.*;
import com.moekr.moocoder.logic.api.GitlabApi;
import com.moekr.moocoder.util.enums.BuildStatus;
import lombok.extern.apachecommons.CommonsLog;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class CommitManager {
	private final CommitDAO commitDAO;
	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;
	private final GitlabApi gitlabApi;

	@Autowired
	public CommitManager(CommitDAO commitDAO, RecordDAO recordDAO, ResultDAO resultDAO, GitlabApi gitlabApi) {
		this.commitDAO = commitDAO;
		this.recordDAO = recordDAO;
		this.resultDAO = resultDAO;
		this.gitlabApi = gitlabApi;
	}

	@Transactional
	public boolean initializeCommit(int resultId, String commitHash) {
		Result result = resultDAO.findById(resultId);
		Exam exam = result == null ? null : result.getExam();
		LocalDateTime now = LocalDateTime.now();
		if (exam != null && exam.getEndAt().isAfter(now)) {
			Set<Problem> problems = calculateProblemsToBuild(result, commitHash);
			Commit newCommit = new Commit();
			newCommit.setHash(commitHash);
			newCommit.setResult(result);
			newCommit = commitDAO.save(newCommit);
			for (Problem problem : problems) {
				Record record = new Record();
				record.setNumber(-1);
				record.setCommit(newCommit);
				record.setProblem(problem);
				recordDAO.save(record);
			}
			result.setLastCommitAt(now);
			resultDAO.save(result);
			return true;
		}
		return false;
	}

	@Transactional
	public boolean finalizeCommit(int resultId) {
		Result result = resultDAO.findById(resultId);
		if (result == null) {
			return false;
		}
		Commit commit = commitDAO.findFirstUnfinishedByResult(result);
		if (commit == null) {
			return false;
		}
		if (commit.getRecords().stream().anyMatch(r -> r.getStatus() == BuildStatus.WAITING || r.getStatus() == BuildStatus.RUNNING)) {
			return false;
		}
		Map<Problem, Record> recordMap = commit.getRecords().stream()
				.collect(Collectors.toMap(Record::getProblem, r -> r));
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
		return true;
	}

	private Set<Problem> calculateProblemsToBuild(Result result, String commitHash) {
		Exam exam = result.getExam();
		Commit lastCommit = commitDAO.findFirstByResultOrderByIdDesc(result);
		String lastHash = lastCommit == null ? exam.getInitialHash() : lastCommit.getHash();
		Map<String, Problem> problemMap = exam.getProblems().stream()
				.collect(Collectors.toMap(Problem::getUniqueName, p -> p));
		Set<Problem> problems;
		try {
			Set<String> difference = gitlabApi.compare(result.getId(), lastHash, commitHash);
			problems = difference.stream()
					.map(problemMap::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
		} catch (GitLabApiException e) {
			problems = new HashSet<>();
		}
		if (problems.isEmpty()) {
			problems = exam.getProblems();
		}
		return problems;
	}
}
