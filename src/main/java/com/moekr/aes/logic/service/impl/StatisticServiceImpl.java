package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.*;
import com.moekr.aes.logic.service.StatisticService;
import com.moekr.aes.logic.vo.model.StatisticModel;
import com.moekr.aes.util.enums.Language;
import com.moekr.aes.util.enums.Role;
import org.springframework.stereotype.Service;

@Service
public class StatisticServiceImpl implements StatisticService {
	private final UserDAO userDAO;
	private final ExaminationDAO examinationDAO;
	private final ResultDAO resultDAO;
	private final RecordDAO recordDAO;
	private final ProblemDAO problemDAO;

	public StatisticServiceImpl(UserDAO userDAO, ExaminationDAO examinationDAO, ResultDAO resultDAO, RecordDAO recordDAO, ProblemDAO problemDAO) {
		this.userDAO = userDAO;
		this.examinationDAO = examinationDAO;
		this.resultDAO = resultDAO;
		this.recordDAO = recordDAO;
		this.problemDAO = problemDAO;
	}

	@Override
	public StatisticModel statistic() {
		StatisticModel statistic = new StatisticModel();
		statistic.setStudentCount(userDAO.countByRole(Role.STUDENT));
		statistic.setTeacherCount(userDAO.countByRole(Role.TEACHER));
		statistic.setExaminationCount((int) examinationDAO.count());
		statistic.setResultCount((int) resultDAO.count());
		statistic.setRecordCount((int) recordDAO.count());
		statistic.setJavaProblemCount(problemDAO.countByLanguage(Language.JAVA));
		statistic.setPythonProblemCount(problemDAO.countByLanguage(Language.PYTHON));
		return statistic;
	}
}
