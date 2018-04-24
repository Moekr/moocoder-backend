package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ExamDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.ExamStatus;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@CommonsLog
public class ClosedExamChecker {
	private final ExamDAO examDAO;
	private final ResultDAO resultDAO;
	private final GitlabApi gitlabApi;
	private final JenkinsApi jenkinsApi;

	public ClosedExamChecker(ExamDAO examDAO, ResultDAO resultDAO, GitlabApi gitlabApi, JenkinsApi jenkinsApi) {
		this.examDAO = examDAO;
		this.resultDAO = resultDAO;
		this.gitlabApi = gitlabApi;
		this.jenkinsApi = jenkinsApi;
	}

	@Transactional
	public void check() {
		List<Exam> examList = examDAO.findAllByStatus(ExamStatus.AVAILABLE);
		LocalDateTime now = LocalDateTime.now();
		examList = examList.stream()
				.filter(e -> e.getEndAt().isBefore(now))
				.peek(e -> e.setStatus(ExamStatus.CLOSED))
				.collect(Collectors.toList());
		examList = examDAO.saveAll(examList);
		for (Exam exam : examList) {
			for (Result result : exam.getResults()) {
				try {
					gitlabApi.archiveProject(result.getId());
					jenkinsApi.deleteJob(result.getId());
					result.setDeleted(true);
				} catch (Exception e) {
					log.error("归档试卷" + result.getId() + "时发生异常" + ToolKit.format(e));
				}
			}
			resultDAO.saveAll(exam.getResults());
		}
	}
}
