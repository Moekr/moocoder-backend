package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.CommitDAO;
import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.entity.*;
import com.moekr.moocoder.logic.api.GitlabApi;
import lombok.extern.apachecommons.CommonsLog;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class CommitInitializer {
	private final CommitDAO commitDAO;
	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;
	private final GitlabApi gitlabApi;

	@Autowired
	public CommitInitializer(CommitDAO commitDAO, RecordDAO recordDAO, ResultDAO resultDAO, GitlabApi gitlabApi) {
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
