package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.util.enums.BuildStatus;
import com.moekr.aes.util.problem.evaluator.Evaluator;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@CommonsLog
public class BuildRecorder {
	private final RecordDAO recordDAO;

	public BuildRecorder(RecordDAO recordDAO) {
		this.recordDAO = recordDAO;
	}

	@Transactional
	public void record(int id, int buildNumber, BuildDetails buildDetails) {
		Record record = recordDAO.findByCommit_Result_IdAndNumber(id, buildNumber);
		if (record == null) return;
		if (buildDetails != null) {
			Evaluator evaluator = record.getProblem().getType().getEvaluator();
			evaluator.evaluate(record, buildDetails);
		} else {
			record.setStatus(BuildStatus.FAILURE);
		}
		recordDAO.save(record);
	}
}
