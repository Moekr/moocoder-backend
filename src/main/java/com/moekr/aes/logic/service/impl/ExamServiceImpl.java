package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ExamDAO;
import com.moekr.aes.data.dao.ProblemDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.AsyncWrapper;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.service.ExamService;
import com.moekr.aes.logic.vo.ExamVO;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.ExamStatus;
import com.moekr.aes.util.enums.UserRole;
import com.moekr.aes.util.exceptions.*;
import com.moekr.aes.web.dto.ExamDTO;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class ExamServiceImpl implements ExamService {
	private static final Sort PAGE_SORT = Sort.by(Sort.Direction.DESC, "id");

	private final UserDAO userDAO;
	private final ProblemDAO problemDAO;
	private final ExamDAO examDAO;
	private final ResultDAO resultDAO;
	private final GitlabApi gitlabApi;
	private final JenkinsApi jenkinsApi;
	private final ExamPaperBuilder paperBuilder;
	private final AsyncWrapper asyncWrapper;

	@Autowired
	public ExamServiceImpl(UserDAO userDAO, ProblemDAO problemDAO, ExamDAO examDAO, ResultDAO resultDAO,
						   GitlabApi gitlabApi, JenkinsApi jenkinsApi, ExamPaperBuilder paperBuilder, AsyncWrapper asyncWrapper) {
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.examDAO = examDAO;
		this.resultDAO = resultDAO;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
		this.paperBuilder = paperBuilder;
		this.asyncWrapper = asyncWrapper;
	}

	@Override
	@Transactional
	public ExamVO create(int userId, ExamDTO examDTO) throws ServiceException {
		User user = userDAO.findById(userId);
		List<Problem> problemList = problemDAO.findAllById(examDTO.getProblemSet());
		Set<Problem> problemSet = problemList.stream()
				.filter(p -> p.getCreator() == null || p.getCreator().getId() == userId)
				.collect(Collectors.toSet());
		if (examDTO.getProblemSet().size() != problemSet.size()) {
			throw new InvalidRequestException("存在无效的题目！");
		}
		Exam exam = new Exam();
		BeanUtils.copyProperties(examDTO, exam);
		exam.setUuid(ToolKit.randomUUID());
		try {
			exam.setId(gitlabApi.createProject(exam.getUuid()));
		} catch (Exception e) {
			throw new ServiceException("创建试题时发生异常" + ToolKit.format(e));
		}
		exam.setCreator(user);
		exam.setProblems(problemSet);
		asyncWrapper.asyncInvoke(() -> paperBuilder.buildPaper(exam.getId()), 1000);
		return new ExamVO(examDAO.save(exam));
	}

	@Override
	public Page<ExamVO> retrievePage(int userId, int page, int limit) {
		User user = userDAO.findById(userId);
		if (user.getRole() == UserRole.TEACHER) {
			return examDAO.findAllByCreator(user, PageRequest.of(page, limit, PAGE_SORT)).map(ExamVO::new);
		} else {
			return resultDAO.findAllByOwner(user, PageRequest.of(page, limit, PAGE_SORT)).map(Result::getExam).map(ExamVO::new);
		}
	}

	@Override
	public ExamVO retrieve(int userId, int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		if (exam.getCreator().getId() != userId) {
			Result result = resultDAO.findByOwner_IdAndExam(userId, exam);
			if (result == null) {
				throw new AccessDeniedException();
			}
		}
		return new ExamVO(exam);
	}

	@Override
	@Transactional
	public ExamVO update(int userId, int examId, ExamDTO examDTO) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		if (exam.getCreator().getId() != userId) {
			throw new AccessDeniedException();
		}
		BeanUtils.copyProperties(examDTO, exam);
		return new ExamVO(examDAO.save(exam));
	}

	@Override
	@Transactional
	public void delete(int userId, int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		if (exam.getCreator().getId() != userId) {
			throw new AccessDeniedException();
		}
		delete(exam);
	}

	@Override
	@Transactional
	public void participate(int userId, int examId) throws ServiceException {
		User user = userDAO.findById(userId);
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		if (exam.getResults().stream().anyMatch(r -> r.getOwner().getId() == userId)) {
			throw new AlreadyInExaminationException();
		} else if (exam.getStatus() == ExamStatus.PREPARING) {
			throw new EntityNotAvailableException("考试正在准备中！");
		} else if (exam.getStatus() == ExamStatus.CLOSED) {
			throw new EntityNotAvailableException("考试已经结束！");
		}
		Result result = new Result();
		try {
			result.setId(gitlabApi.forkProject(userId, examId, user.getNamespace()));
			jenkinsApi.createJob(result.getId());
		} catch (Exception e) {
			throw new ServiceException("复制试卷时发生异常[" + e.getMessage() + "]");
		}
		result.setOwner(user);
		result.setExam(exam);
		resultDAO.save(result);
	}

	@Override
	public Page<ExamVO> retrievePage(int page, int limit) {
		return examDAO.findAll(PageRequest.of(page, limit, PAGE_SORT)).map(ExamVO::new);
	}

	@Override
	public ExamVO retrieve(int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		return new ExamVO(exam);
	}

	@Override
	public ExamVO update(int examId, ExamDTO examDTO) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		BeanUtils.copyProperties(examDTO, exam);
		return new ExamVO(examDAO.save(exam));
	}

	@Override
	@Transactional
	public void delete(int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		delete(exam);
	}
	
	private void delete(Exam exam) {
		for (Result result : exam.getResults()) {
			try {
				gitlabApi.deleteProject(result.getId());
				if (!result.isDeleted()) {
					jenkinsApi.deleteJob(result.getId());
				}
			} catch (Exception e) {
				log.error("删除试卷#" + result.getId() + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
			}
		}
		try {
			gitlabApi.deleteProject(exam.getId());
		} catch (Exception e) {
			log.error("删除试题#" + exam.getId() + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
		}
		examDAO.delete(exam);
	}
}
