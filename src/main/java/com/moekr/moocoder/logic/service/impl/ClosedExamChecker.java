package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.dao.ExamDAO;
import com.moekr.moocoder.data.dao.ResultDAO;
import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.logic.api.GitlabApi;
import com.moekr.moocoder.logic.api.JenkinsApi;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.enums.ExamStatus;
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
		// XXX 等待30分钟所有提交均完成测试
		LocalDateTime now = LocalDateTime.now().plusMinutes(30);
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
