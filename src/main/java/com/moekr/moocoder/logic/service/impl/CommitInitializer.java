package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.CommitDAO;
import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.entity.Commit;
import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.data.entity.Result;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@CommonsLog
public class CommitInitializer {
	private final CommitDAO commitDAO;
	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;

	public CommitInitializer(CommitDAO commitDAO, RecordDAO recordDAO, ResultDAO resultDAO) {
		this.commitDAO = commitDAO;
		this.recordDAO = recordDAO;
		this.resultDAO = resultDAO;
	}

	@Transactional
	public boolean initializeCommit(int resultId, String commitHash) {
		Result result = resultDAO.findById(resultId);
		LocalDateTime now = LocalDateTime.now();
		if (result != null && result.getExam().getEndAt().isAfter(now)) {
			Commit commit = new Commit();
			commit.setHash(commitHash);
			commit.setResult(result);
			commit = commitDAO.save(commit);
			for (Problem problem : commit.getResult().getExam().getProblems()) {
				Record record = new Record();
				record.setNumber(-1);
				record.setCommit(commit);
				record.setProblem(problem);
				recordDAO.save(record);
			}
			result.setLastCommitAt(now);
			resultDAO.save(result);
			return true;
		}
		return false;
	}
}
