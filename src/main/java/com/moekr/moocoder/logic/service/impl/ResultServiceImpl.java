package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.ExamDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.dao.UserDAO;
import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.logic.service.ResultService;
import com.moekr.moocoder.logic.vo.ResultVO;
import com.moekr.moocoder.util.exceptions.AccessDeniedException;
import com.moekr.moocoder.util.exceptions.Asserts;
import com.moekr.moocoder.util.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResultServiceImpl implements ResultService {
	private final ExamDAO examDAO;
	private final ResultDAO resultDAO;
	private final UserDAO userDAO;

	@Autowired
	public ResultServiceImpl(ExamDAO examDAO, ResultDAO resultDAO, UserDAO userDAO) {
		this.examDAO = examDAO;
		this.resultDAO = resultDAO;
		this.userDAO = userDAO;
	}

	@Override
	public ResultVO retrieve(int userId, int resultId) throws ServiceException {
		Result result = resultDAO.findById(resultId);
		Asserts.notNull(result, "所选的成绩不存在");
		if (result.getOwner().getId() == userId) {
			return new ResultVO(result, true);
		} else if (result.getExam().getCreator().getId() == userId) {
			return new ResultVO(result, true);
		}
		throw new AccessDeniedException();
	}

	@Override
	public List<ResultVO> retrieveByOwner(int userId) throws ServiceException {
		User user = userDAO.findById(userId);
		Asserts.notNull(user, "所选用户不存在");
		return resultDAO.findAllByOwner(user).stream().map(r -> new ResultVO(r, false)).collect(Collectors.toList());
	}

	@Override
	public List<ResultVO> retrieveByExam(int userId, int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选的考试不存在");
		User user = userDAO.findById(userId);
		return resultDAO.findAllByExamAndOwnerIsNot(exam, user).stream().map(r -> new ResultVO(r, false)).collect(Collectors.toList());
	}

	@Override
	public ResultVO retrieve(int resultId) throws ServiceException {
		Result result = resultDAO.findById(resultId);
		Asserts.notNull(result, "所选的成绩不存在");
		return new ResultVO(result, true);
	}

	@Override
	public List<ResultVO> retrieveByExam(int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选的考试不存在");
		return resultDAO.findAllByExamAndOwnerIsNot(exam, exam.getCreator()).stream().map(r -> new ResultVO(r, false)).collect(Collectors.toList());
	}
}
