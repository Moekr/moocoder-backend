package com.moekr.moocoder.util.problem.evaluator;

import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.logic.api.vo.CoverageResult;
import com.moekr.moocoder.logic.api.vo.CoverageResultElement;
import com.moekr.moocoder.util.enums.BuildStatus;
import com.offbytwo.jenkins.model.BuildResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public class BasicCoverageEvaluator extends AbstractEvaluator {
	public BasicCoverageEvaluator() {
		super();
	}

	public BasicCoverageEvaluator(int timeout) {
		super(timeout);
	}

	@Override
	public void evaluate(Record record, BuildDetails buildDetails) {
		super.evaluate(record, buildDetails);
		if (record.getStatus() == BuildStatus.SUCCESS && record.getScore() < 100) {
			record.setStatus(BuildStatus.UNSTABLE);
		}
	}

	@Override
	protected BuildStatus status(BuildDetails details) {
		if (details.getCoverageResult() == null) {
			details.setBuildResult(BuildResult.FAILURE);
		}
		return super.status(details);
	}

	@Override
	protected int evaluate(BuildDetails buildDetails, Set<Record.Failure> failures) {
		CoverageResult coverageResult = buildDetails.getCoverageResult();
		if (coverageResult == null) {
			return 0;
		}
		int totalRatio = 0;
		StringBuilder builder = new StringBuilder();
		for (CoverageResultElement element : coverageResult.getElements()) {
			builder.append(element.getName()).append(": ").append(element.getNumerator()).append("/").append(element.getDenominator()).append("\n");
			totalRatio = totalRatio + element.getRatio();
		}
		if (totalRatio < coverageResult.getElements().size() * 100) {
			Record.Failure failure = new Record.Failure();
			failure.setName("Coverage");
			failure.setDetails(StringUtils.EMPTY);
			failure.setTrace(builder.toString());
			failures.add(failure);
		}
		return totalRatio / coverageResult.getElements().size();
	}
}
