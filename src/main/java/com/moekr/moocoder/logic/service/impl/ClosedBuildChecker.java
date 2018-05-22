package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.logic.api.JenkinsApi;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.logic.service.NotifyService;
import com.moekr.moocoder.util.enums.BuildStatus;
import com.moekr.moocoder.util.enums.ProblemType;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class ClosedBuildChecker {
	private static final int THRESHOLD = 3;

	private final RecordDAO recordDAO;
	private final JenkinsApi jenkinsApi;
	private final NotifyService notifyService;

	private Map<Integer, Integer> history;

	public ClosedBuildChecker(RecordDAO recordDAO, JenkinsApi jenkinsApi, NotifyService notifyService) {
		this.recordDAO = recordDAO;
		this.jenkinsApi = jenkinsApi;
		this.notifyService = notifyService;
		this.history = new HashMap<>();
	}

	@Transactional
	public void check() {
		Set<Record> records = recordDAO.findAllByStatus(BuildStatus.RUNNING);
		Map<Integer, Integer> newHistory = records.stream().collect(Collectors.toMap(Record::getId, r -> 1));
		history.forEach((key, value) -> newHistory.computeIfPresent(key, (k, v) -> value + v));
		history = newHistory;
		history.entrySet().stream().filter(e -> e.getValue() > THRESHOLD).forEach(e -> invokeCallback(e.getKey()));
	}

	private void invokeCallback(int recordId) {
		Record record = recordDAO.findById(recordId);
		if (record.getStatus() != BuildStatus.RUNNING) return;
		Result result = record.getCommit().getResult();
		ProblemType type = record.getProblem().getType();
		try {
			BuildDetails details = jenkinsApi.fetchBuildDetails(result.getId(), record.getNumber(), type.getTarget(), false);
			// 表明构建仍在进行，此时不应该触发回调
			if (details.getBuildResult() == null) return;
		} catch (Exception e) {
			return;
		}
		notifyService.callback(result.getId(), record.getNumber());
	}
}
