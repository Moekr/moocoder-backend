package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.TransactionWrapper;
import com.moekr.aes.data.TransactionWrapper.SafeMethod;
import com.moekr.aes.data.dao.ExaminationDAO;
import com.moekr.aes.data.dao.ProblemDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.dao.UserDAO;
import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.service.ExaminationService;
import com.moekr.aes.logic.storage.StorageProvider;
import com.moekr.aes.logic.vo.model.ExaminationModel;
import com.moekr.aes.util.*;
import com.moekr.aes.util.AesProperties.Gitlab;
import com.moekr.aes.util.enums.Role;
import com.moekr.aes.web.dto.form.ChangeExaminationForm;
import com.moekr.aes.web.dto.form.CreateExaminationForm;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.http.HttpStatus;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@CommonsLog
public class ExaminationServiceImpl implements ExaminationService {
	private final AesProperties properties;
	private final UserDAO userDAO;
	private final ProblemDAO problemDAO;
	private final ExaminationDAO examinationDAO;
	private final ResultDAO resultDAO;
	private final GitlabApi gitlabApi;
	private final JenkinsApi jenkinsApi;
	private final TransactionWrapper wrapper;
	private final StorageProvider storageProvider;

	@Autowired
	public ExaminationServiceImpl(AesProperties properties, UserDAO userDAO, ProblemDAO problemDAO, ExaminationDAO examinationDAO, ResultDAO resultDAO,
								  GitlabApi gitlabApi, JenkinsApi jenkinsApi, TransactionWrapper wrapper, StorageProvider storageProvider) {
		this.properties = properties;
		this.userDAO = userDAO;
		this.problemDAO = problemDAO;
		this.examinationDAO = examinationDAO;
		this.resultDAO = resultDAO;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
		this.wrapper = wrapper;
		this.storageProvider = storageProvider;
	}

	@Override
	public ExaminationModel findById(int userId, int examinationId) {
		Examination examination = examinationDAO.findById(examinationId).orElse(null);
		Asserts.isTrue(examination != null, HttpStatus.SC_NOT_FOUND);
		Asserts.isTrue(examination.getResultSet().stream().anyMatch(r -> r.getUser().getId() == userId), HttpStatus.SC_FORBIDDEN);
		return new ExaminationModel(examination);
	}

	@Override
	public List<ExaminationModel> findAll(int userId) {
		User user = userDAO.findById(userId).orElse(null);
		Assert.notNull(user, "找不到用户");
		return user.getResultSet().stream()
				.map(Result::getExamination)
				.map(ExaminationModel::new)
				.sorted((o1, o2) -> o2.getId() - o1.getId())
				.collect(Collectors.toList());
	}

	@Override
	public List<ExaminationModel> findAll() {
		return examinationDAO.findAll().stream()
				.map(ExaminationModel::new)
				.sorted((o1, o2) -> o2.getId() - o1.getId())
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void create(int userId, int problemId, CreateExaminationForm form) {
		User user = userDAO.findById(userId).orElse(null);
		Assert.notNull(user, "找不到用户");
		Assert.isTrue(user.getRole() == Role.TEACHER, "没有权限");
		Problem problem = problemDAO.findById(problemId).orElse(null);
		Asserts.isTrue(problem != null, "题目不存在！");
		LocalDateTime startAt;
		LocalDateTime endAt;
		try {
			startAt = ToolKit.parse(form.getStartAt());
			endAt = ToolKit.parse(form.getEndAt());
		} catch (DateTimeParseException e) {
			throw new ServiceException("时间格式不正确！");
		}
		Asserts.isTrue(endAt.isAfter(startAt), "考试结束时间必须晚于开始时间！");
		Asserts.isTrue(endAt.isAfter(LocalDateTime.now()), "考试结束时间必须晚于当前时间！");
		String project = ToolKit.randomUUID();
		int projectId = gitlabApi.createProject(project);
		byte[] content;
		try {
			content = storageProvider.fetch(problem.getFile() + ".zip");
		} catch (IOException e) {
			throw new ServiceException("下载题目文件失败！");
		}
		Gitlab gitlab = properties.getGitlab();
		String url = gitlab.getHost() + "/" + gitlab.getUsername() + "/" + project;
		try {
			GitUtils.pushFromZipArchive(content, url, gitlab.getUsername(), gitlab.getToken());
		} catch (IOException | GitAPIException e) {
			throw new ServiceException("Push试题失败！");
		}
		Examination examination = new Examination();
		examination.setId(projectId);
		examination.setName(form.getName());
		examination.setProject(project);
		examination.setClosed(false);
		examination.setStartAt(startAt);
		examination.setEndAt(endAt);
		examination.setUser(user);
		examination.setProblem(problem);
		examination.setCreatedAt(LocalDateTime.now());
		examinationDAO.save(examination);
		participate(userId, projectId);
	}

	@Override
	@Transactional
	public void participate(int userId, int examinationId) {
		User user = userDAO.findById(userId).orElse(null);
		Assert.notNull(user, "找不到用户");
		Examination examination = examinationDAO.findById(examinationId).orElse(null);
		Asserts.isTrue(examination != null, "考试不存在！");
		Asserts.isTrue(examination.getResultSet().stream().noneMatch(r -> r.getUser().getId() == userId), "已经加入了这场考试！");
		Asserts.isTrue(!examination.getClosed(), "考试已经结束！");
		int projectId = gitlabApi.forkProject(userId, examinationId, user.getNamespace());
		Problem problem = examination.getProblem();
		String webHook = jenkinsApi.createJob(projectId, user.getUsername(), examination.getProject(), problem.getFile(), problem.getLanguage());
		gitlabApi.createWebHook(projectId, webHook);
		Result result = new Result();
		result.setId(projectId);
		result.setScore(0);
		result.setUser(user);
		result.setExamination(examination);
		resultDAO.save(result);
	}

	@Override
	@Transactional
	public void change(int userId, int examinationId, ChangeExaminationForm form) {
		User user = userDAO.findById(userId).orElse(null);
		Assert.notNull(user, "找不到用户");
		Assert.isTrue(user.getRole() == Role.TEACHER, "没有权限");
		Examination examination = examinationDAO.findById(examinationId).orElse(null);
		Asserts.isTrue(examination != null, "考试不存在！");
		Asserts.isTrue(examination.getUser().getId() == userId, "没有权限！");
		Asserts.isTrue(!examination.getClosed(), "考试已经结束！");
		LocalDateTime startAt;
		LocalDateTime endAt;
		try {
			startAt = ToolKit.parse(form.getStartAt());
			endAt = ToolKit.parse(form.getEndAt());
		} catch (DateTimeParseException e) {
			throw new ServiceException("时间格式不正确！");
		}
		Asserts.isTrue(endAt.isAfter(startAt), "考试结束时间必须晚于开始时间！");
		examination.setStartAt(startAt);
		examination.setEndAt(endAt);
		examinationDAO.save(examination);
	}

	@Scheduled(cron = "5 * * * * *")
	protected void scheduledCheckClosedExamination() {
		wrapper.wrap((SafeMethod) this::checkClosedExamination);
	}

	private void checkClosedExamination() {
		List<Examination> examinationList = examinationDAO.findAllByClosed(false);
		examinationList = examinationList.stream()
				.filter(e -> e.getEndAt().isBefore(LocalDateTime.now()))
				.peek(e -> e.setClosed(true))
				.collect(Collectors.toList());
		examinationList = examinationDAO.saveAll(examinationList);
		for (Examination examination : examinationList) {
			for (Result result : examination.getResultSet()) {
				try {
					gitlabApi.archiveProject(result.getId());
				} catch (ServiceException e) {
					log.warn("归档GitLab项目[ID=" + result.getId() + "]时发生异常：" + e.getMessage());
				}
				if (properties.getJenkins().getDeleteAfterClose()) {
					try {
						jenkinsApi.deleteJob(String.valueOf(result.getId()));
					} catch (ServiceException e) {
						log.warn("删除Jenkins项目[ID=" + result.getId() + "]时发生异常：" + e.getMessage());
					}
				}
			}
		}
	}
}
