package com.moekr.moocoder.util.problem.evaluator;

import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.util.enums.BuildStatus;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEvaluator implements Evaluator {
	private final long timeout;

	public AbstractEvaluator() {
		this(1);
	}

	public AbstractEvaluator(int timeout) {
		Assert.isTrue(timeout >= 1, "超时时间必须大于等于1分钟");
		this.timeout = timeout * DateUtils.MILLIS_PER_MINUTE;
	}

	@Override
	public void evaluate(Record record, BuildDetails buildDetails) {
		record.setStatus(status(buildDetails));
		record.setConsoleOutput(consoleOutput(buildDetails));
		Set<Record.Failure> failures = new HashSet<>();
		record.setScore(evaluate(buildDetails, failures));
		record.setFailures(failures);
	}

	protected BuildStatus status(BuildDetails details) {
		switch (details.getBuildResult()) {
			case SUCCESS:
				return BuildStatus.SUCCESS;
			case UNSTABLE:
				return BuildStatus.UNSTABLE;
			case ABORTED:
			case FAILURE:
				// FIXME 当出现排队情况时该种方法检测是否超时不准确
				if (details.getDuration() > timeout) {
					return BuildStatus.TIMEOUT;
				}
		}
		return BuildStatus.FAILURE;
	}

	protected String consoleOutput(BuildDetails details) {
		StringBuilder builder = new StringBuilder();
		Arrays.stream(details.getConsoleOutput().split("\n"))
				.filter(line -> !line.startsWith("[Pipeline]"))
				.forEach(line -> builder.append(line).append('\n'));
		return builder.toString();
	}

	protected abstract int evaluate(BuildDetails buildDetails, Set<Record.Failure> failures);
}
