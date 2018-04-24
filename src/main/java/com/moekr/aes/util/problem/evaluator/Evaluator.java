package com.moekr.aes.util.problem.evaluator;

import com.moekr.aes.data.entity.Record;
import com.moekr.aes.logic.api.vo.BuildDetails;

public interface Evaluator {
	void evaluate(Record record, BuildDetails buildDetails);
}
