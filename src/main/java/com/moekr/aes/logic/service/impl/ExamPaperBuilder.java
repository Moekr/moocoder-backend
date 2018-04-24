package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.TransactionWrapper;
import com.moekr.aes.data.dao.ExamDAO;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.logic.storage.StorageProvider;
import com.moekr.aes.util.enums.ExamStatus;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

@Component
@CommonsLog
public class ExamPaperBuilder {
	private static final String TEMP_PREFIX = "AES-CODE-";

	private final ExamDAO examDAO;
	private final TransactionWrapper wrapper;
	private final StorageProvider provider;
	private final GitProcessHelper helper;

	@Autowired
	public ExamPaperBuilder(ExamDAO examDAO, TransactionWrapper wrapper, StorageProvider provider, GitProcessHelper helper) {
		this.examDAO = examDAO;
		this.wrapper = wrapper;
		this.provider = provider;
		this.helper = helper;
	}

	@Async
	public void asyncBuildPaper(Exam exam) {
		try {
			wrapper.wrap((TransactionWrapper.Method) () -> buildPaper(exam));
		} catch (Exception e) {
			log.error("为#" + exam.getId() + "构建试卷失败[" + e.getClass() + "]: " + e.getMessage());
		}
	}

	@Transactional
	public void buildPaper(Exam exam) throws IOException, GitAPIException {
		if (exam.getStatus() != ExamStatus.PREPARING) {
			return;
		}
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			releaseCode(exam.getProblemSet(), tempDir);
			helper.push(tempDir, exam.getUuid());
			exam.setStatus(ExamStatus.AVAILABLE);
			examDAO.save(exam);
		} catch (Exception e) {
			exam.setStatus(ExamStatus.UNAVAILABLE);
			examDAO.save(exam);
			throw e;
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private void releaseCode(Set<Problem> problemSet, File codeDir) throws IOException {
		FileUtils.cleanDirectory(codeDir);
		for (Problem problem : problemSet) {
			File problemDir = new File(codeDir, problem.getName());
			if (!problemDir.mkdir()) {
				throw new IOException("创建临时文件夹失败！");
			}
			byte[] content = provider.fetch(problem.getId() + ".zip");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
			ZipUtil.unpack(inputStream, problemDir);
			for (String path : problem.getPrivateFiles()) {
				if (File.separatorChar != '/') {
					path = path.replace('/', File.separatorChar);
				}
				File privateFile = new File(problemDir, path);
				if (privateFile.exists()) {
					if (!privateFile.delete()) {
						log.error("删除私有文件[" + privateFile.getAbsolutePath() + "]失败！");
					}
				}
			}
		}
	}
}
