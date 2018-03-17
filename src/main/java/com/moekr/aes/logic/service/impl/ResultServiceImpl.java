package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.service.ResultService;
import com.moekr.aes.logic.vo.model.ResultModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
	public ResultModel findByExamination(int userId, int examinationId) {
		Result result = resultDAO.findOne(userId, examinationId);
		Assert.notNull(result, "找不到成绩");
		return new ResultModel(result);
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
		resultList.forEach(r -> scoreData.put(r.getUser().getUsername(), r.getScore()));
		return scoreData;
	}
}
