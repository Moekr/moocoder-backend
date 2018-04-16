package com.moekr.aes.logic.service;

import java.util.Map;

public interface ResultService {
	String scoreDistribution(int examinationId);

	Map<String, Integer> scoreData(int examinationId);
}
