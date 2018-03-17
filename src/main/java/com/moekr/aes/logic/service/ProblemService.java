package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.model.ProblemModel;

import java.util.List;

public interface ProblemService {
	List<ProblemModel> findAll();

	ProblemModel findById(int problemId);

	void upload(int userId, byte[] content);
}
