package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ExamDAO;
import com.moekr.aes.data.dao.ProblemDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
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
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
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
	private final DockerImageBuilder imageBuilder;

	@Autowired
	public ExamServiceImpl(UserDAO userDAO, ProblemDAO problemDAO, ExamDAO examDAO, ResultDAO resultDAO,
						   GitlabApi gitlabApi, JenkinsApi jenkinsApi, ExamPaperBuilder paperBuilder, DockerImageBuilder imageBuilder) {
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.examDAO = examDAO;
		this.resultDAO = resultDAO;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
		this.paperBuilder = paperBuilder;
		this.imageBuilder = imageBuilder;
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
		String uuid = ToolKit.randomUUID();
		int id;
		try {
			id = gitlabApi.createProject(uuid);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab项目时发生异常[" + e.getMessage() + "]");
		}
		Exam exam = new Exam();
		BeanUtils.copyProperties(examDTO, exam);
		exam.setId(id);
		exam.setUuid(uuid);
		exam.setCreator(user);
		exam.setProblemSet(problemSet);
		exam = examDAO.save(exam);
		paperBuilder.asyncBuildPaper(exam);
		return new ExamVO(exam);
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
	public ExamVO retrieve(int userId, int examinationId) throws ServiceException {
		Exam exam = examDAO.findById(examinationId);
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
	public ExamVO update(int userId, int examinationId, ExamDTO examDTO) throws EntityNotFoundException, AccessDeniedException {
		Exam exam = examDAO.findById(examinationId);
		Asserts.notNull(exam, "所选考试不存在");
		if (exam.getCreator().getId() != userId) {
			throw new AccessDeniedException();
		}
		BeanUtils.copyProperties(examDTO, exam);
		return new ExamVO(examDAO.save(exam));
	}

	@Override
	@Transactional
	public void delete(int userId, int examinationId) throws EntityNotFoundException, AccessDeniedException {
		Exam exam = examDAO.findById(examinationId);
		Asserts.notNull(exam, "所选考试不存在");
		if (exam.getCreator().getId() != userId) {
			throw new AccessDeniedException();
		}
		delete(examinationId);
	}

	@Override
	@Transactional
	public void participate(int userId, int examinationId) throws ServiceException {
		User user = userDAO.findById(userId);
		Exam exam = examDAO.findById(examinationId);
		Asserts.notNull(exam, "所选考试不存在");
		if (exam.getResultSet().stream().anyMatch(r -> r.getOwner().getId() == userId)) {
			throw new AlreadyInExaminationException();
		}
		if (exam.getStatus() == ExamStatus.PREPARING) {
			throw new EntityNotAvailableException("考试正在准备中！");
		} else if (exam.getStatus() == ExamStatus.CLOSED) {
			throw new EntityNotAvailableException("考试已经结束！");
		}
		int id;
		try {
			id = gitlabApi.forkProject(userId, examinationId, user.getNamespace());
		} catch (GitLabApiException e) {
			throw new ServiceException("复制GitLab项目时发生异常[" + e.getMessage() + "]");
		}
		try {
			jenkinsApi.createJob(id);
		} catch (IOException e) {
			throw new ServiceException("创建Jenkins项目时发生异常[" + e.getMessage() + "]");
		}
		try {
			gitlabApi.createWebHook(id);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建WebHook时发生异常[" + e.getMessage() + "]");
		}
		Result result = new Result();
		result.setId(id);
		result.setOwner(user);
		result.setExam(exam);
		resultDAO.save(result);
	}

	@Override
	public Page<ExamVO> retrievePage(int page, int limit) {
		return examDAO.findAll(PageRequest.of(page, limit, PAGE_SORT)).map(ExamVO::new);
	}

	@Override
	public ExamVO retrieve(int examinationId) throws ServiceException {
		Exam exam = examDAO.findById(examinationId);
		Asserts.notNull(exam, "所选考试不存在");
		return new ExamVO(exam);
	}

	@Override
	public ExamVO update(int examinationId, ExamDTO examDTO) throws ServiceException {
		Exam exam = examDAO.findById(examinationId);
		Asserts.notNull(exam, "所选考试不存在");
		BeanUtils.copyProperties(examDTO, exam);
		return new ExamVO(examDAO.save(exam));
	}

	@Override
	@Transactional
	public void delete(int examinationId) throws EntityNotFoundException {
		Exam exam = examDAO.findById(examinationId);
		Asserts.notNull(exam, "所选考试不存在");
		for (Result result : exam.getResultSet()) {
			try {
				gitlabApi.deleteProject(result.getId());
			} catch (Exception e) {
				log.error("删除GitLab项目#" + result.getId() + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
			}
			if (!result.isDeleted()) {
				try {
					jenkinsApi.deleteJob(result.getId());
				} catch (Exception e) {
					log.error("删除Jenkins项目#" + result.getId() + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
				}
			}
		}
		try {
			gitlabApi.deleteProject(exam.getId());
		} catch (Exception e) {
			log.error("删除GitLab项目#" + exam.getId() + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
		}
		examDAO.delete(exam);
	}
}
