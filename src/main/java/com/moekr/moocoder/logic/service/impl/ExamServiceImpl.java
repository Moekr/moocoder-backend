package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.ExamDAO;
import com.moekr.moocoder.data.dao.ProblemDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.dao.UserDAO;
import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.logic.AsyncWrapper;
import com.moekr.moocoder.logic.api.GitlabApi;
import com.moekr.moocoder.logic.api.JenkinsApi;
import com.moekr.moocoder.logic.service.ExamService;
import com.moekr.moocoder.logic.vo.ExamVO;
import com.moekr.moocoder.logic.vo.JoinedExamVO;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.enums.ExamStatus;
import com.moekr.moocoder.util.enums.UserRole;
import com.moekr.moocoder.util.exceptions.*;
import com.moekr.moocoder.web.dto.ExamDTO;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
	private final ApplicationProperties properties;

	@Autowired
	public ExamServiceImpl(UserDAO userDAO, ProblemDAO problemDAO, ExamDAO examDAO, ResultDAO resultDAO,
						   GitlabApi gitlabApi, JenkinsApi jenkinsApi, ExamPaperBuilder paperBuilder, AsyncWrapper asyncWrapper, ApplicationProperties properties) {
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.examDAO = examDAO;
		this.resultDAO = resultDAO;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
		this.paperBuilder = paperBuilder;
		this.asyncWrapper = asyncWrapper;
		this.properties = properties;
	}

	@Override
	@Transactional
	public ExamVO create(int userId, ExamDTO examDTO) throws ServiceException {
		User user = userDAO.findById(userId);
		List<Problem> problemList = problemDAO.findAllById(examDTO.getProblems());
		Set<Problem> problemSet = problemList.stream()
				.filter(p -> p.getCreator() == null || p.getCreator().getId() == userId)
				.collect(Collectors.toSet());
		if (examDTO.getProblems().size() != problemSet.size()) {
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
	public Page<ExamVO> retrievePage(int userId, int page, int limit, boolean joined, ExamStatus status) {
		Pageable pageable = PageRequest.of(page, limit, PAGE_SORT);
		Page<Exam> pageResult;
		if (joined) {
			pageResult = examDAO.findAllJoined(userId, pageable);
		} else if (status == null) {
			pageResult = examDAO.findAll(pageable);
		} else {
			pageResult = examDAO.findAllByActualStatus(status, pageable);
		}
		return pageResult.map(e -> convert(userId, e));
	}

	@Override
	public Page<ExamVO> retrievePage(int userId, int page, int limit, ExamStatus status) {
		User user = userDAO.findById(userId);
		Pageable pageable = PageRequest.of(page, limit, PAGE_SORT);
		Page<Exam> pageResult;
		if (status == null) {
			pageResult = examDAO.findAllByCreator(user, pageable);
		} else {
			pageResult = examDAO.findAllByCreatorAndActualStatus(user, status, pageable);
		}
		return pageResult.map(e -> convert(user, e));
	}

	@Override
	public Page<ExamVO> retrievePage(int page, int limit, ExamStatus status) {
		Pageable pageable = PageRequest.of(page, limit, PAGE_SORT);
		Page<Exam> pageResult;
		if (status == null) {
			pageResult = examDAO.findAll(pageable);
		} else {
			pageResult = examDAO.findAllByActualStatus(status, pageable);
		}
		return pageResult.map(JoinedExamVO::new);
	}

	@Override
	public ExamVO retrieve(int userId, int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		return convert(userId, exam);
	}

	@Override
	public ExamVO retrieve(int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		return new JoinedExamVO(exam);
	}

	private ExamVO convert(int userId, Exam exam) {
		return convert(userDAO.findById(userId), exam);
	}

	private ExamVO convert(User user, Exam exam) {
		Result result = resultDAO.findByOwnerAndExam(user, exam);
		if (result != null) {
			String url = properties.getGitlab().getGitProxy() + "/" + result.getOwner().getUsername() + "/" + exam.getUuid();
			return new JoinedExamVO(exam, url, result);
		} else if (exam.getCreator().getId().equals(user.getId())) {
			return new JoinedExamVO(exam);
		} else {
			return new ExamVO(exam);
		}
	}

	@Override
	@Transactional
	public ExamVO update(int userId, int examId, ExamDTO examDTO) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		checkAccess(userId, exam);
		exam.setStartAt(examDTO.getStartAt());
		exam.setEndAt(examDTO.getEndAt());
		return new ExamVO(examDAO.save(exam));
	}

	@Override
	@Transactional
	public void delete(int userId, int examId) throws ServiceException {
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		checkAccess(userId, exam);
		for (Result result : exam.getResults()) {
			try {
				gitlabApi.deleteProject(result.getId());
				if (!result.isDeleted()) {
					jenkinsApi.deleteJob(result.getId());
				}
			} catch (Exception e) {
				log.error("删除试卷" + result.getId() + "时发生异常" + ToolKit.format(e));
			}
		}
		try {
			gitlabApi.deleteProject(exam.getId());
		} catch (Exception e) {
			log.error("删除试题" + exam.getId() + "时发生异常" + ToolKit.format(e));
		}
		examDAO.delete(exam);
	}

	@Override
	@Transactional
	public void join(int userId, int examId) throws ServiceException {
		User user = userDAO.findById(userId);
		Exam exam = examDAO.findById(examId);
		Asserts.notNull(exam, "所选考试不存在");
		if (resultDAO.findByOwnerAndExam(user, exam) != null) {
			throw new AlreadyInExaminationException();
		} else if (user.getRole() == UserRole.TEACHER && !user.equals(exam.getCreator())) {
			throw new AccessDeniedException();
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
			throw new ServiceException("复制试卷时发生异常" + ToolKit.format(e));
		}
		result.setOwner(user);
		result.setExam(exam);
		resultDAO.save(result);
	}

	private void checkAccess(int userId, Exam exam) throws AccessDeniedException {
		if (userId != 0) {
			checkAccess(userDAO.findById(userId), exam);
		}
	}

	private void checkAccess(User user, Exam exam) throws AccessDeniedException {
		if (user != null && !exam.getCreator().equals(user)) {
			throw new AccessDeniedException();
		}
	}
}
