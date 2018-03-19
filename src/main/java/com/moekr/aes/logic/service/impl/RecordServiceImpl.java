package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.TransactionWrapper;
import com.moekr.aes.data.TransactionWrapper.SafeMethod;
import com.moekr.aes.data.dao.ExaminationDAO;
import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.service.RecordService;
import com.moekr.aes.logic.vo.model.RecordModel;
import com.moekr.aes.util.Asserts;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.enums.Language;
import com.moekr.aes.util.enums.Role;
import com.offbytwo.jenkins.model.*;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class RecordServiceImpl implements RecordService {
	private final ResultDAO resultDAO;
	private final RecordDAO recordDAO;
	private final ExaminationDAO examinationDAO;
	private final JenkinsApi jenkinsApi;
	private final TransactionWrapper wrapper;

	@Autowired
	public RecordServiceImpl(ResultDAO resultDAO, RecordDAO recordDAO, ExaminationDAO examinationDAO, JenkinsApi jenkinsApi, TransactionWrapper wrapper) {
		this.resultDAO = resultDAO;
		this.recordDAO = recordDAO;
		this.examinationDAO = examinationDAO;
		this.jenkinsApi = jenkinsApi;
		this.wrapper = wrapper;
	}

	@Override
	public RecordModel findById(int userId, int recordId) {
		Record record = recordDAO.findById(recordId).orElse(null);
		Asserts.isTrue(record != null, HttpStatus.SC_NOT_FOUND);
		Asserts.isTrue(record.getResult().getUser().getId() == userId, HttpStatus.SC_FORBIDDEN);
		return new RecordModel(record);
	}

	@Override
	public List<RecordModel> findAll(int userId) {
		return recordDAO.findAllByUser(userId).stream()
				.map(RecordModel::new)
				.sorted((o1, o2) -> o2.getId() - o1.getId())
				.collect(Collectors.toList());
	}

	@Override
	public Map<String, Integer> failData(int examinationId) {
		Examination examination = examinationDAO.findById(examinationId).orElse(null);
		Assert.notNull(examination, "找不到考试");
		List<Record> recordList = new ArrayList<>();
		examination.getResultSet().stream()
				.filter(r -> r.getUser().getRole() == Role.STUDENT)
				.map(Result::getRecordSet)
				.forEach(recordList::addAll);
		Map<String, Integer> failData = new HashMap<>();
		recordList.stream()
				.map(RecordModel::new)
				.forEach(r -> r.getFailSet().forEach(f -> failData.put(f.getName(), failData.getOrDefault(f.getName(), 0) + 1)));
		return failData;
	}

	@Override
	@Async
	public void asyncRecord(int id, int buildNumber) {
		wrapper.wrap((SafeMethod) () -> record(id, buildNumber));
	}

	private void record(int id, int buildNumber) {
		Result result = resultDAO.findById(id).orElse(null);
		if (result == null) {
			log.warn("对应于编号 " + id + " 的成绩记录不存在！");
			return;
		}
		if (result.getExamination().getClosed()) return;
		List<TestSuites> testSuitesList = null;
		if (result.getExamination().getProblem().getLanguage() == Language.JAVA) {
			TestReport testReport;
			try {
				testReport = jenkinsApi.fetchTestReport(id, buildNumber);
			} catch (ServiceException e) {
				testReport = null;
			}
			if (testReport != null) {
				testSuitesList = new ArrayList<>();
				testReport.getChildReports().stream()
						.map(TestChildReport::getResult)
						.map(TestResult::getSuites)
						.forEach(testSuitesList::addAll);
			} else {
				log.warn("对应于编号 " + id + " 的构建编号为 " + buildNumber + " 的Maven测试结果不存在！");
			}
		} else {
			TestResult testResult;
			try {
				testResult = jenkinsApi.fetchTestResult(id, buildNumber);
			} catch (ServiceException e) {
				testResult = null;
			}
			if (testResult != null) {
				testSuitesList = new ArrayList<>(testResult.getSuites());
			} else {
				log.warn("对应于编号 " + id + " 的构建编号为 " + buildNumber + " 的项目测试结果不存在！");
			}
		}
		if (testSuitesList != null) {
			JSONArray pass = new JSONArray();
			JSONArray fail = new JSONArray();
			for (TestSuites testSuites : testSuitesList) {
				for (TestCase testCase : testSuites.getCases()) {
					String caseName = testCase.getClassName() + "." + testCase.getName();
					if (StringUtils.equalsAnyIgnoreCase(testCase.getStatus(), "PASSED", "FIXED")) {
						pass.put(caseName);
					} else {
						JSONObject object = new JSONObject();
						object.put("name", caseName);
						object.put("details", StringUtils.defaultString(testCase.getErrorDetails()));
						object.put("trace", StringUtils.defaultString(testCase.getErrorStackTrace()));
						fail.put(object);
					}
				}
			}
			Record record = new Record();
			record.setCompiled(true);
			record.setScore((int) (100.0 * pass.length() / Math.max(pass.length() + fail.length(), 1)));
			record.setPass(pass.toString());
			record.setFail(fail.toString());
			record.setCreatedAt(LocalDateTime.now());
			record.setResult(result);
			record = recordDAO.save(record);
			if (record.getScore() > result.getScore()) {
				result.setScore(record.getScore());
				resultDAO.save(result);
			}
		} else {
			Record record = new Record();
			record.setCompiled(false);
			record.setScore(0);
			record.setPass("[]");
			record.setFail("[]");
			record.setCreatedAt(LocalDateTime.now());
			record.setResult(result);
			recordDAO.save(record);
		}
	}

}
