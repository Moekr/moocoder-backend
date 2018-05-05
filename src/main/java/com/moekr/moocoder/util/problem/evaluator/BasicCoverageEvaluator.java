package com.moekr.moocoder.util.problem.evaluator;

import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.logic.api.vo.CoberturaElement;
import com.moekr.moocoder.logic.api.vo.CoberturaResult;
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
	protected BuildStatus status(BuildDetails details) {
		if (details.getCoberturaResult() == null) {
			details.setBuildResult(BuildResult.FAILURE);
		}
		return super.status(details);
	}

	@Override
	protected int evaluate(BuildDetails buildDetails, Set<Record.Failure> failures) {
		CoberturaResult coberturaResult = buildDetails.getCoberturaResult();
		if (coberturaResult == null) {
			return 0;
		}
		int totalRatio = 0;
		StringBuilder builder = new StringBuilder();
		for (CoberturaElement element : coberturaResult.getElements()) {
			builder.append(element.getName()).append(": ").append(element.getNumerator()).append("/").append(element.getDenominator()).append("\n");
			totalRatio = totalRatio + element.getRatio();
		}
		if (totalRatio < coberturaResult.getElements().size() * 100) {
			Record.Failure failure = new Record.Failure();
			failure.setName("Cobertura Coverage");
			failure.setDetails(StringUtils.EMPTY);
			failure.setTrace(builder.toString());
			failures.add(failure);
		}
		return totalRatio / coberturaResult.getElements().size();
	}
}
