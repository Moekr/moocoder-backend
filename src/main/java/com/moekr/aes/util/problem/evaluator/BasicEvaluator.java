package com.moekr.aes.util.problem.evaluator;

import com.moekr.aes.data.entity.Record.Failure;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.util.enums.BuildStatus;
import com.offbytwo.jenkins.model.BuildResult;
import com.offbytwo.jenkins.model.TestCase;
import com.offbytwo.jenkins.model.TestResult;
import com.offbytwo.jenkins.model.TestSuites;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public class BasicEvaluator extends AbstractEvaluator {
	private static final String[] PASS_STATUS = {"PASSED", "FIXED"};

	public BasicEvaluator() {
		this(1);
	}

	public BasicEvaluator(int timeout) {
		super(timeout);
	}

	protected BuildStatus status(BuildDetails details) {
		if (details.getTestResult() == null) {
			details.setBuildResult(BuildResult.FAILURE);
		}
		return super.status(details);
	}

	protected int evaluate(BuildDetails buildDetails, Set<Failure> failures) {
		TestResult testResult = buildDetails.getTestResult();
		if (testResult == null) {
			return 0;
		}
		int passCount = 0;
		int totalCount = 0;
		for (TestSuites testSuites : testResult.getSuites()) {
			for (TestCase testCase : testSuites.getCases()) {
				String caseName = testCase.getClassName() + "." + testCase.getName();
				if (StringUtils.equalsAnyIgnoreCase(testCase.getStatus(), PASS_STATUS)) {
					passCount++;
				} else {
					Failure failure = new Failure();
					failure.setName(caseName);
					failure.setDetails(StringUtils.defaultString(testCase.getErrorDetails()));
					failure.setTrace(StringUtils.defaultString(testCase.getErrorStackTrace()));
					failures.add(failure);
				}
				totalCount++;
			}
		}
		return (int) (100.0 * passCount / totalCount);
	}
}
