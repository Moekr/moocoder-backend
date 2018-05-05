package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.*;
import com.moekr.moocoder.logic.service.StatisticService;
import com.moekr.moocoder.logic.vo.StatisticVO;
import com.moekr.moocoder.util.enums.ProblemType;
import com.moekr.moocoder.util.enums.UserRole;
import org.springframework.stereotype.Service;

@Service
public class StatisticServiceImpl implements StatisticService {
	private final UserDAO userDAO;
	private final ExamDAO examDAO;
	private final ResultDAO resultDAO;
	private final RecordDAO recordDAO;
	private final ProblemDAO problemDAO;

	public StatisticServiceImpl(UserDAO userDAO, ExamDAO examDAO, ResultDAO resultDAO, RecordDAO recordDAO, ProblemDAO problemDAO) {
		this.userDAO = userDAO;
		this.examDAO = examDAO;
		this.resultDAO = resultDAO;
		this.recordDAO = recordDAO;
		this.problemDAO = problemDAO;
	}

	@Override
	public StatisticVO statistic() {
		StatisticVO statistic = new StatisticVO();
		statistic.setStudentCount(userDAO.countByRole(UserRole.STUDENT));
		statistic.setTeacherCount(userDAO.countByRole(UserRole.TEACHER));
		statistic.setExaminationCount((int) examDAO.count());
		statistic.setResultCount((int) resultDAO.count());
		statistic.setRecordCount((int) recordDAO.count());
		statistic.setJavaProblemCount(problemDAO.countByType(ProblemType.JAVA));
		statistic.setPythonProblemCount(problemDAO.countByType(ProblemType.PYTHON));
		return statistic;
	}
}
