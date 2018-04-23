package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.CommitDAO;
import com.moekr.aes.data.dao.RecordDAO;
import com.moekr.aes.data.dao.ResultDAO;
import com.moekr.aes.data.entity.Commit;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.logic.service.NotifyService;
import com.moekr.aes.util.enums.BuildStatus;
import com.offbytwo.jenkins.model.QueueItem;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
@CommonsLog
public class NotifyServiceImpl implements NotifyService {
	private final CommitDAO commitDAO;
	private final RecordDAO recordDAO;
	private final ResultDAO resultDAO;
	private final JenkinsApi jenkinsApi;
	private final BuildReportRecorder recorder;

	@Autowired
	public NotifyServiceImpl(CommitDAO commitDAO, RecordDAO recordDAO, ResultDAO resultDAO, JenkinsApi jenkinsApi, BuildReportRecorder recorder) {
		this.commitDAO = commitDAO;
		this.recordDAO = recordDAO;
		this.resultDAO = resultDAO;
		this.jenkinsApi = jenkinsApi;
		this.recorder = recorder;
	}

	@Override
	@Transactional
	public void webHook(int id, String commitHash) {
		Result result = resultDAO.findById(id);
		if (result != null) {
			Commit commit = new Commit();
			commit.setHash(commitHash);
			commit.setResult(result);
			commit = commitDAO.save(commit);
			Set<Record> records = new HashSet<>();
			for (Problem problem : commit.getResult().getExam().getProblemSet()) {
				Record record = new Record();
				record.setNumber(-1);
				record.setCommit(commit);
				record.setProblem(problem);
				records.add(record);
			}
			recordDAO.saveAll(records);
		}
		nextBuild(id);
	}

	@Override
	public void callback(int id, int buildNumber) {
		BuildDetails buildDetails = null;
		try {
			buildDetails = jenkinsApi.fetchBuildDetails(id, buildNumber);
		} catch (Exception e) {
			log.error("获取项目#" + id + "的构建记录#" + buildNumber + "时发生异常[" + e.getClass() + "]: " + e.getMessage());
		}
		if (buildDetails != null) {
			recorder.record(id, buildDetails);
		}
		nextBuild(id);
	}

	private void nextBuild(int id) {
		Commit commit = commitDAO.findFirstByResult_IdAndFinishedOrderByIdAsc(id, false);
		while (commit != null) {
			Record record = commit.getRecords().stream().filter(r -> r.getStatus() == BuildStatus.WAITING).findFirst().orElse(null);
			if (record != null) {
				try {
					QueueItem item = jenkinsApi.invokeBuild(id, null);
					record.setNumber(item.getExecutable().getNumber().intValue());
					record.setStatus(BuildStatus.RUNNING);
				} catch (IOException e) {
					log.error("触发构建时发生异常");
					record.setStatus(BuildStatus.FAILURE);
				}
				recordDAO.save(record);
				break;
			} else {
				commit.setFinished(true);
				commitDAO.save(commit);
				commit = commitDAO.findFirstByResult_IdAndFinishedOrderByIdAsc(id, false);
			}
		}
	}
}
