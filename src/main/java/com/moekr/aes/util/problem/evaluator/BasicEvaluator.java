package com.moekr.aes.util.problem.evaluator;

import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Record.Failure;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.util.enums.BuildStatus;
import com.offbytwo.jenkins.model.TestCase;
import com.offbytwo.jenkins.model.TestResult;
import com.offbytwo.jenkins.model.TestSuites;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.Assert;

import java.util.HashSet;
import java.util.Set;

public class BasicEvaluator implements Evaluator {
	private static final String[] PASS_STATUS = {"PASSED", "FIXED"};

	private final long timeout;

	public BasicEvaluator() {
		this(1);
	}

	public BasicEvaluator(int timeout) {
		Assert.isTrue(timeout >= 1, "超时时间必须大于等于1分钟");
		this.timeout = timeout * DateUtils.MILLIS_PER_MINUTE;
	}

	@Override
	public void evaluate(Record record, BuildDetails buildDetails) {
		record.setStatus(status(buildDetails));
		record.setConsoleOutput(buildDetails.getConsoleOutput());
		Set<Failure> failures = new HashSet<>();
		record.setScore(evaluate(buildDetails, failures));
		record.setFailures(failures);
	}

	protected BuildStatus status(BuildDetails details) {
		if (details.getTestResult() == null || details.getBuildResult() == null) {
			return BuildStatus.FAILURE;
		}
		switch (details.getBuildResult()) {
			case SUCCESS:
				return BuildStatus.SUCCESS;
			case UNSTABLE:
				return BuildStatus.UNSTABLE;
			case FAILURE:
				if (details.getDuration() > timeout) {
					return BuildStatus.TIMEOUT;
				} else {
					return BuildStatus.FAILURE;
				}
		}
		return BuildStatus.FAILURE;
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
