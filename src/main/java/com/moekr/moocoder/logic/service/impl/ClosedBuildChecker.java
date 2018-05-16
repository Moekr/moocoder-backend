package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.RecordDAO;
import com.moekr.moocoder.data.entity.Record;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.logic.service.NotifyService;
import com.moekr.moocoder.util.enums.BuildStatus;
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
	private final NotifyService notifyService;

	private Map<Integer, Integer> history;

	public ClosedBuildChecker(RecordDAO recordDAO, NotifyService notifyService) {
		this.recordDAO = recordDAO;
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
		if (record == null || record.getStatus() != BuildStatus.RUNNING) {
			return;
		}
		Result result = record.getCommit().getResult();
		notifyService.callback(result.getId(), record.getNumber());
	}
}
