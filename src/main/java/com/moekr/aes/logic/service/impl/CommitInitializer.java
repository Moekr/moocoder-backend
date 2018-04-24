package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.CommitDAO;
import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Commit;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Result;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
	public void initializeCommit(int resultId, String commitHash) {
		Result result = resultDAO.findById(resultId);
		if (result != null) {
			Commit commit = new Commit();
			commit.setHash(commitHash);
			commit.setResult(result);
			commit = commitDAO.save(commit);
			for (Problem problem : commit.getResult().getExam().getProblemSet()) {
				Record record = new Record();
				record.setNumber(-1);
				record.setCommit(commit);
				record.setProblem(problem);
				commit.getRecords().add(recordDAO.save(record));
			}
		}
	}
}
