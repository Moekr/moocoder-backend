package com.moekr.aes.logic.service.impl;

import com.google.common.base.Ascii;
import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.logic.api.vo.CoberturaElement;
import com.moekr.aes.logic.api.vo.CoberturaResult;
import com.moekr.aes.util.enums.BuildStatus;
import com.moekr.aes.util.enums.ProblemType;
import com.offbytwo.jenkins.model.TestCase;
import com.offbytwo.jenkins.model.TestResult;
import com.offbytwo.jenkins.model.TestSuites;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class BuildReportRecorder {
	private static final long TIMEOUT = 3 * DateUtils.MILLIS_PER_MINUTE;
	private static final String[] PASS_STATUS = {"PASS", "FIXED"};
	private static final int FAILURE_MAX_LENGTH = 65500;

	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;

	public BuildReportRecorder(RecordDAO recordDAO, ResultDAO resultDAO) {
		this.recordDAO = recordDAO;
		this.resultDAO = resultDAO;
	}

	@Transactional
	public void record(int id, int buildNumber) {
		Result result = resultDAO.findById(id).orElse(null);
		if (result != null) {
			Record record = new Record();
			record.setNumber(buildNumber);
			record.setResult(result);
			recordDAO.save(record);
		} else {
			log.error("编号#" + id + "的成绩记录不存在！");
		}
	}

	@Transactional
	public void record(int id, BuildDetails buildDetails) {
		Record record = recordDAO.findByResultIdAndNumber(id, buildDetails.getNumber()).orElse(null);
		if (record != null) {
			BuildStatus status = status(buildDetails);
			JSONArray failureArray = new JSONArray();
			int testScore = evaluateTest(buildDetails.getTestResult(), failureArray);
			int coverageScore = evaluateCoverage(buildDetails.getCoberturaResult(), failureArray);
			Set<Problem> problemSet = record.getResult().getExamination().getProblemSet();
			int coverageCount = (int) problemSet.stream().map(Problem::getType).filter(ProblemType::isCoverage).count();
			int testCount = problemSet.size() - coverageCount;
			record.setStatus(status);
			record.setScore((testScore * testCount + coverageScore * coverageCount) / problemSet.size());
			record.setFailure(formatFailure(failureArray));
		} else {
			log.error("编号#" + id + "的提交记录不存在！");
		}
	}

	private BuildStatus status(BuildDetails details) {
		if (details.getBuildResult() == null && details.getCoberturaResult() == null) {
			return BuildStatus.FAILURE;
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

	private int evaluateTest(TestResult testResult, JSONArray failureArray) {
		int passCount = 0;
		int totalCount = 0;
		for (TestSuites testSuites : testResult.getSuites()) {
			for (TestCase testCase : testSuites.getCases()) {
				String caseName = testCase.getClassName() + "." + testCase.getName();
				if (StringUtils.equalsAnyIgnoreCase(testCase.getStatus(), PASS_STATUS)) {
					passCount++;
				} else {
					JSONObject object = new JSONObject();
					object.put("name", caseName);
					object.put("details", StringUtils.defaultString(testCase.getErrorDetails()));
					object.put("trace", StringUtils.defaultString(testCase.getErrorStackTrace()));
					failureArray.put(object);
				}
				totalCount++;
			}
		}
		return (int) (100.0 * passCount / totalCount);
	}

	private int evaluateCoverage(CoberturaResult coberturaResult, JSONArray failureArray) {
		int totalRatio = 0;
		StringBuilder builder = new StringBuilder();
		for (CoberturaElement element : coberturaResult.getElements()) {
			builder.append(element.getName()).append(": ").append(element.getNumerator()).append("/").append(element.getDenominator()).append("\n");
			totalRatio = totalRatio + element.getRatio();
		}
		if (totalRatio < coberturaResult.getElements().size() * 100) {
			JSONObject object = new JSONObject();
			object.put("name", "Cobertura Coverage");
			object.put("details", StringUtils.EMPTY);
			object.put("trace", builder.toString());
			failureArray.put(object);
		}
		return (int) (100.0 * totalRatio / coberturaResult.getElements().size());
	}

	private String formatFailure(JSONArray failureArray) {
		String failure = failureArray.toString();
		if (failure.length() < FAILURE_MAX_LENGTH) {
			return failure;
		}
		List<JSONObject> failureList = new ArrayList<>();
		for (Object object : failureArray) {
			if (object instanceof JSONObject) {
				failureList.add((JSONObject) object);
			}
		}
		List<Integer> traceLengthList = failureList.stream().map(o -> o.optString("trace")).map(String::length).sorted().collect(Collectors.toList());
		int totalLength = traceLengthList.stream().reduce((a, b) -> a + b).orElse(0);
		int targetLength = FAILURE_MAX_LENGTH - (failure.length() - totalLength);
		int truncateLength = 0;
		for (int traceLength : traceLengthList) {
			int currentLength = traceLengthList.stream().map(a -> Math.min(a, traceLength)).reduce((a, b) -> a + b).orElse(0);
			if (currentLength <= targetLength) {
				truncateLength = currentLength;
			} else {
				break;
			}
		}
		int finalTruncateLength = truncateLength;
		failureList.forEach(f -> f.put("trace", Ascii.truncate(f.optString("trace"), finalTruncateLength, "[达到长度限制]")));
		return failureArray.toString();
	}
}
