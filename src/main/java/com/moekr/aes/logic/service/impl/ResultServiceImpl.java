package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.service.ResultService;
import com.moekr.aes.logic.vo.ResultVO;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.Asserts;
import com.moekr.aes.util.exceptions.ServiceException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResultServiceImpl implements ResultService {
	private final ResultDAO resultDAO;

	@Autowired
	public ResultServiceImpl(ResultDAO resultDAO) {
		this.resultDAO = resultDAO;
	}

	@Override
	public ResultVO retrieve(int userId, int resultId) throws ServiceException {
		Result result = resultDAO.findById(resultId);
		Asserts.notNull(result, "所选的成绩不存在");
		if (result.getOwner().getId() == userId) {
			return new ResultVO(result);
		} else if (result.getExamination().getOwner().getId() == userId) {
			return new ResultVO(result);
		}
		throw new AccessDeniedException();
	}

	@Override
	public ResultVO retrieve(int resultId) throws ServiceException {
		Result result = resultDAO.findById(resultId);
		Asserts.notNull(result, "所选的成绩不存在");
		return new ResultVO(result);
	}

	@Override
	public String scoreDistribution(int examinationId) {
		List<Integer> scoreList = resultDAO.findAllByExamination(examinationId).stream().map(Result::getScore).collect(Collectors.toList());
		Map<Integer, Integer> scoreDistribution = scoreList.stream().collect(Collectors.toMap(s -> (s / 10), s -> 1, (a, b) -> a + b));
		JSONArray array = new JSONArray();
		for (int scoreLevel = 0; scoreLevel < 10; scoreLevel++) {
			JSONObject object = new JSONObject();
			int lowestScore = scoreLevel * 10;
			object.put("x", lowestScore + "-" + (lowestScore + 9));
			object.put("y", scoreDistribution.getOrDefault(scoreLevel, 0));
			array.put(object);
		}
		JSONObject object = new JSONObject();
		object.put("x", "100");
		object.put("y", scoreDistribution.getOrDefault(10, 0));
		array.put(object);
		return array.toString();
	}

	@Override
	public Map<String, Integer> scoreData(int examinationId) {
		List<Result> resultList = resultDAO.findAllByExamination(examinationId);
		Map<String, Integer> scoreData = new HashMap<>();
		resultList.forEach(r -> scoreData.put(r.getOwner().getUsername(), r.getScore()));
		return scoreData;
	}
}
