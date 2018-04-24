package com.moekr.aes.logic.service.impl;

import com.google.common.base.Ascii;
import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Record.Failure;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.logic.api.vo.CoberturaElement;
import com.moekr.aes.logic.api.vo.CoberturaResult;
import com.moekr.aes.util.enums.BuildStatus;
import com.offbytwo.jenkins.model.TestCase;
import com.offbytwo.jenkins.model.TestResult;
import com.offbytwo.jenkins.model.TestSuites;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@CommonsLog
public class BuildReportRecorder {
	private static final long TIMEOUT = 3 * DateUtils.MILLIS_PER_MINUTE;
	private static final String[] PASS_STATUS = {"PASSED", "FIXED"};
	private static final int TEXT_MAX_LENGTH = 65500;
	private static final String TRUNCATE_INDICATOR = "[达到长度限制]";

	private final RecordDAO recordDAO;

	public BuildReportRecorder(RecordDAO recordDAO) {
		this.recordDAO = recordDAO;
	}

	@Transactional
	public void record(int id, BuildDetails buildDetails) {
		Record record = recordDAO.findByCommit_Result_IdAndNumber(id, buildDetails.getNumber());
		if (record != null) {
			record.setStatus(status(buildDetails));
			record.setConsoleOutput(Ascii.truncate(buildDetails.getConsoleOutput(), TEXT_MAX_LENGTH, TRUNCATE_INDICATOR));
			Set<Failure> failures = new HashSet<>();
			if (record.getProblem().getType().isCoverage()) {
				record.setScore(evaluateCoverage(buildDetails.getCoberturaResult(), failures));
			} else {
				record.setScore(evaluateTest(buildDetails.getTestResult(), failures));
			}
			record.setFailures(failures);
		} else {
			log.error("编号#" + id + "/" + buildDetails.getNumber() + "的提交记录不存在！");
		}
	}

	private BuildStatus status(BuildDetails details) {
		if (details.getBuildResult() == null && details.getCoberturaResult() == null) {
			return BuildStatus.FAILURE;
		}
		if (details.getBuildResult() == null) {
			return BuildStatus.SUCCESS;
		}
		switch (details.getBuildResult()) {
			case SUCCESS:
				return BuildStatus.SUCCESS;
			case UNSTABLE:
				return BuildStatus.UNSTABLE;
			case FAILURE:
				if (details.getDuration() > TIMEOUT) {
					return BuildStatus.TIMEOUT;
				} else {
					return BuildStatus.FAILURE;
				}
		}
		return BuildStatus.FAILURE;
	}

	private int evaluateTest(TestResult testResult, Set<Failure> failures) {
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

	private int evaluateCoverage(CoberturaResult coberturaResult, Set<Failure> failures) {
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
			Failure failure = new Failure();
			failure.setName("Cobertura Coverage");
			failure.setDetails(StringUtils.EMPTY);
			failure.setTrace(builder.toString());
			failures.add(failure);
		}
		return totalRatio / coberturaResult.getElements().size();
	}
}