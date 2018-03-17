package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.model.RecordModel;

import java.util.List;
import java.util.Map;

public interface RecordService {
	RecordModel findById(int userId, int recordId);

	List<RecordModel> findAll(int userId);

	Map<String, Integer> failData(int examinationId);

	void asyncRecord(int id, int buildNumber);
}
