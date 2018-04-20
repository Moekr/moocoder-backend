package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.ResultVO;
import com.moekr.aes.util.exceptions.ServiceException;

import java.util.Map;

public interface ResultService {
	ResultVO retrieve(int userId, int resultId) throws ServiceException;

	ResultVO retrieve(int resultId) throws ServiceException;

	String scoreDistribution(int examinationId);

	Map<String, Integer> scoreData(int examinationId);
}
