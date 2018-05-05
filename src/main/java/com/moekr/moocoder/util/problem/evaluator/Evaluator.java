package com.moekr.moocoder.util.problem.evaluator;

import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.logic.api.vo.BuildDetails;

public interface Evaluator {
	void evaluate(Record record, BuildDetails buildDetails);
}
