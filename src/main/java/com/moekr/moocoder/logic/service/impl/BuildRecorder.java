package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.util.enums.BuildStatus;
import com.moekr.moocoder.util.problem.evaluator.Evaluator;
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
