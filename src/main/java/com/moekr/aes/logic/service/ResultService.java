package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.model.ResultModel;

import java.util.Map;

public interface ResultService {
	ResultModel findByExamination(int userId, int examinationId);

	String scoreDistribution(int examinationId);

	Map<String, Integer> scoreData(int examinationId);
}
