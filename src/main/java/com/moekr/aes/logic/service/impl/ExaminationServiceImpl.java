package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.*;
import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.service.ExaminationService;
import com.moekr.aes.logic.vo.ExaminationVO;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.ExaminationStatus;
import com.moekr.aes.util.exceptions.*;
import com.moekr.aes.web.dto.ExaminationDTO;
import lombok.extern.apachecommons.CommonsLog;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class ExaminationServiceImpl implements ExaminationService {
	private final UserDAO userDAO;
	private final ProblemDAO problemDAO;
	private final ExaminationDAO examinationDAO;
	private final ResultDAO resultDAO;
	private final RecordDAO recordDAO;
	private final GitlabApi gitlabApi;
	private final JenkinsApi jenkinsApi;
	private final PaperBuilder paperBuilder;
	private final DockerImageBuilder imageBuilder;

	@Autowired
	public ExaminationServiceImpl(UserDAO userDAO, ProblemDAO problemDAO, ExaminationDAO examinationDAO, ResultDAO resultDAO, RecordDAO recordDAO,
								  GitlabApi gitlabApi, JenkinsApi jenkinsApi, PaperBuilder paperBuilder, DockerImageBuilder imageBuilder) {
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.examinationDAO = examinationDAO;
		this.resultDAO = resultDAO;
		this.recordDAO = recordDAO;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
		this.paperBuilder = paperBuilder;
		this.imageBuilder = imageBuilder;
	}

	@Override
	@Transactional
	public ExaminationVO create(int userId, ExaminationDTO examinationDTO) throws ServiceException {
		User user = userDAO.findById(userId);
		List<Problem> problemList = problemDAO.findAllById(examinationDTO.getProblemSet());
		Set<Problem> problemSet = problemList.stream()
				.filter(p -> p.getOwner() == null || p.getOwner().getId() == userId)
				.collect(Collectors.toSet());
		if (examinationDTO.getProblemSet().size() != problemSet.size()) {
			throw new InvalidRequestException("存在无效的题目！");
		}
		String uuid = ToolKit.randomUUID();
		int id;
		try {
			id = gitlabApi.createProject(uuid);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab项目时发生异常[" + e.getMessage() + "]");
		}
		Examination examination = new Examination();
		BeanUtils.copyProperties(examinationDTO, examination);
		examination.setId(id);
		examination.setUuid(uuid);
		examination.setOwner(user);
		examination.setProblemSet(problemSet);
		examination = examinationDAO.save(examination);
		try {
			paperBuilder.buildPaper(examination);
		} catch (IOException | GitAPIException e) {
			throw new ServiceException("构建试题时发生异常[" + e.getMessage() + "]");
		}
		imageBuilder.asyncBuildDockerImage(examination);
		return new ExaminationVO(examination);
	}

	@Override
	@Transactional
	public ExaminationVO update(int userId, int examinationId, ExaminationDTO examinationDTO) throws EntityNotFoundException, AccessDeniedException {
		Examination examination = examinationDAO.findById(examinationId);
		Asserts.notNull(examination, "所选考试不存在");
		if (examination.getOwner().getId() != userId) {
			throw new AccessDeniedException();
		}
		BeanUtils.copyProperties(examinationDTO, examination);
		return new ExaminationVO(examinationDAO.save(examination));
	}

	@Override
	@Transactional
	public void delete(int userId, int examinationId) throws EntityNotFoundException, AccessDeniedException {
		Examination examination = examinationDAO.findById(examinationId);
		Asserts.notNull(examination, "所选考试不存在");
		if (examination.getOwner().getId() != userId) {
			throw new AccessDeniedException();
		}
		delete(examinationId);
	}

	@Override
	@Transactional
	public void delete(int examinationId) throws EntityNotFoundException {
		Examination examination = examinationDAO.findById(examinationId);
		Asserts.notNull(examination, "所选考试不存在");
		// TODO: 细化事务控制
		for (Result result : examination.getResultSet()) {
			try {
				gitlabApi.deleteProject(result.getId());
			} catch (Exception e) {
				log.error(e);
			}
			if (!result.isDeleted()) {
				try {
					jenkinsApi.deleteJob(result.getId());
				} catch (Exception e) {
					log.error(e);
				}
			}
			recordDAO.deleteAll(result.getRecordSet());
			resultDAO.delete(result);
		}
		try {
			gitlabApi.deleteProject(examination.getId());
		} catch (Exception e) {
			log.error(e);
		}
		examinationDAO.delete(examination);
	}

	@Override
	@Transactional
	public void participate(int userId, int examinationId) throws ServiceException {
		User user = userDAO.findById(userId);
		Examination examination = examinationDAO.findById(examinationId);
		Asserts.notNull(examination, "所选考试不存在");
		if (examination.getResultSet().stream().anyMatch(r -> r.getOwner().getId() == userId)) {
			throw new AlreadyInExaminationException();
		}
		if (examination.getStatus() == ExaminationStatus.PREPARING) {
			throw new EntityNotAvailableException("考试正在准备中！");
		} else if (examination.getStatus() == ExaminationStatus.CLOSED) {
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
		result.setExamination(examination);
		resultDAO.save(result);
	}
}
