package com.moekr.moocoder.util.problem.evaluator;

import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.data.entity.Record.Failure;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.util.enums.BuildStatus;

public class PythonEvaluator extends BasicEvaluator {
	private static final String FAILURE_CASE = "nose.failure.Failure.runTest";

	public PythonEvaluator() {
		super();
	}

	public PythonEvaluator(int timeout) {
		super(timeout);
	}

	@Override
	public void evaluate(Record record, BuildDetails buildDetails) {
		super.evaluate(record, buildDetails);
		if (record.getFailures().stream().map(Failure::getName).anyMatch(n -> n.equals(FAILURE_CASE))) {
			record.setStatus(BuildStatus.FAILURE);
			record.setScore(0);
		}
	}
}
