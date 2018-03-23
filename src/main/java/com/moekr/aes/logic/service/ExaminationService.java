package com.moekr.aes.logic.service;

import com.moekr.aes.logic.vo.model.ExaminationModel;
import com.moekr.aes.web.dto.form.ChangeExaminationForm;
import com.moekr.aes.web.dto.form.CreateExaminationForm;

import java.util.List;

public interface ExaminationService {
	default boolean canAccess(int userId, int examinationId) {
		try {
			return findById(userId, examinationId) != null;
		} catch (Exception e) {
			return false;
		}
	}

	ExaminationModel findById(int userId, int examinationId);

	List<ExaminationModel> findAll(int userId);

	List<ExaminationModel> findAll();

	void create(int userId, int problemId, CreateExaminationForm form);

	void participate(int userId, int examinationId);

	void change(int userId, int examinationId, ChangeExaminationForm form);

	void delete(int examinationId);
}
