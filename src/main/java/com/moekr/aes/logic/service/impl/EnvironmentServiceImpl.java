package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.service.EnvironmentService;
import com.moekr.aes.util.AesProperties;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class EnvironmentServiceImpl implements EnvironmentService {
	private final AesProperties properties;
	private final ResultDAO resultDAO;

	@Autowired
	public EnvironmentServiceImpl(AesProperties properties, ResultDAO resultDAO) {
		this.properties = properties;
		this.resultDAO = resultDAO;
	}

	@Override
	public Map<String, String> env(int resultId) {
		Result result = resultDAO.findById(resultId).orElse(null);
		if (result == null) return Collections.emptyMap();
		Examination examination = result.getExamination();
		Map<String, String> env = new HashMap<>();
		env.put("GIT_URL", properties.getGitlab().getHost() + "/" + result.getOwner().getUsername() + "/" + examination.getUuid());
		env.put("DOCKER_IMAGE", properties.getDocker().getRegistry() + "/" + examination.getUuid() + ":" + examination.getVersion());
		StringBuilder builder = new StringBuilder();
		builder.append("#!/bin/bash\n");
		for (Problem problem : examination.getProblemSet()) {
			JSONArray publicFileArray = new JSONArray(problem.getPublicFiles());
			for (int index = 0; index < publicFileArray.length(); index++) {
				String publicFile = publicFileArray.optString(index, null);
				if (publicFile != null) {
					builder.append("cp --parents ").append(problem.getName()).append(publicFile).append(" /var/ws/code/ || :\n");
				}
			}
		}
		for (Problem problem : examination.getProblemSet()) {
			builder.append(problem.getType().runScript(problem.getName()));
		}
		env.put("EXECUTE_SHELL", builder.toString());
		return env;
	}
}
