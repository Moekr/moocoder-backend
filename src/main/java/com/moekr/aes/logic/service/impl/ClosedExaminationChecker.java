package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.TransactionWrapper;
import com.moekr.aes.data.dao.ExaminationDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.util.enums.ExaminationStatus;
import lombok.extern.apachecommons.CommonsLog;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class ClosedExaminationChecker {
	private final ExaminationDAO examinationDAO;
	private final ResultDAO resultDAO;
	private final GitlabApi gitlabApi;
	private final JenkinsApi jenkinsApi;
	private final TransactionWrapper wrapper;

	public ClosedExaminationChecker(ExaminationDAO examinationDAO, ResultDAO resultDAO, GitlabApi gitlabApi, JenkinsApi jenkinsApi, TransactionWrapper wrapper) {
		this.examinationDAO = examinationDAO;
		this.resultDAO = resultDAO;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
		this.wrapper = wrapper;
	}

	@Scheduled(cron = "5 * * * * *")
	protected void scheduledCheckClosedExamination() {
		wrapper.wrap((TransactionWrapper.SafeMethod) this::checkClosedExamination);
	}

	private void checkClosedExamination() {
		List<Examination> examinationList = examinationDAO.findAllByStatus(ExaminationStatus.AVAILABLE);
		examinationList = examinationList.stream()
				.filter(e -> e.getEndAt().isBefore(LocalDateTime.now()))
				.peek(e -> e.setStatus(ExaminationStatus.CLOSED))
				.collect(Collectors.toList());
		examinationList = examinationDAO.saveAll(examinationList);
		// TODO: 细化事务处理
		for (Examination examination : examinationList) {
			for (Result result : examination.getResultSet()) {
				try {
					gitlabApi.archiveProject(result.getId());
				} catch (GitLabApiException e) {
					log.error("归档GitLab项目#" + result.getId() + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
				}
				try {
					jenkinsApi.deleteJob(result.getId());
					result.setDeleted(true);
				} catch (IOException e) {
					log.error("删除Jenkins项目#" + result.getId() + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
				}
			}
			resultDAO.saveAll(examination.getResultSet());
		}
	}
}
