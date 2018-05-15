package com.moekr.moocoder.util.problem.evaluator;

import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.logic.api.vo.MutationResult;
import com.moekr.moocoder.util.enums.BuildStatus;
import com.offbytwo.jenkins.model.BuildResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public class BasicMutationEvaluator extends AbstractEvaluator {
	public BasicMutationEvaluator() {
		super();
	}

	public BasicMutationEvaluator(int timeout) {
		super(timeout);
	}

	@Override
	protected BuildStatus status(BuildDetails details) {
		MutationResult mutationResult = details.getMutationResult();
		if (mutationResult == null) {
			details.setBuildResult(BuildResult.FAILURE);
		} else if (mutationResult.getMutations() > mutationResult.getDetectedMutations()) {
			details.setBuildResult(BuildResult.UNSTABLE);
		}
		return super.status(details);
	}

	@Override
	protected int evaluate(BuildDetails buildDetails, Set<Record.Failure> failures) {
		MutationResult mutationResult = buildDetails.getMutationResult();
		if (mutationResult == null) {
			return 0;
		}
		if (mutationResult.getMutations() == mutationResult.getDetectedMutations()) {
			return 100;
		} else {
			Record.Failure failure = new Record.Failure();
			failure.setName("Mutation");
			failure.setDetails(StringUtils.EMPTY);
			failure.setTrace("Mutations: " + mutationResult.getMutations() + "\n"
					+ "Detected Mutations: " + mutationResult.getDetectedMutations());
			failures.add(failure);
			return mutationResult.getDetectedMutations() * 100 / mutationResult.getMutations();
		}
	}
}
