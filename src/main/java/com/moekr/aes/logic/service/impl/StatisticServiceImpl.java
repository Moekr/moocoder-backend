package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.*;
import com.moekr.aes.logic.service.StatisticService;
import com.moekr.aes.logic.vo.StatisticVO;
import com.moekr.aes.util.enums.ProblemType;
import com.moekr.aes.util.enums.UserRole;
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
	public StatisticVO statistic() {
		StatisticVO statistic = new StatisticVO();
		statistic.setStudentCount(userDAO.countByRole(UserRole.STUDENT));
		statistic.setTeacherCount(userDAO.countByRole(UserRole.TEACHER));
		statistic.setExaminationCount((int) examinationDAO.count());
		statistic.setResultCount((int) resultDAO.count());
		statistic.setRecordCount((int) recordDAO.count());
		statistic.setJavaProblemCount(problemDAO.countByType(ProblemType.JAVA));
		statistic.setPythonProblemCount(problemDAO.countByType(ProblemType.PYTHON));
		return statistic;
	}
}
