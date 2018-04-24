package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ExamDAO;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.logic.storage.StorageProvider;
import com.moekr.aes.util.enums.ExamStatus;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final StorageProvider provider;
	private final GitHelper helper;

	@Autowired
	public ExamPaperBuilder(ExamDAO examDAO, GitHelper helper, StorageProvider provider) {
		this.examDAO = examDAO;
		this.helper = helper;
		this.provider = provider;
	}

	@Transactional
	public void buildPaper(int examId) throws IOException, GitAPIException {
		Exam exam = examDAO.findById(examId);
		if (exam.getStatus() != ExamStatus.PREPARING) return;
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			releaseCode(exam.getProblems(), tempDir);
			helper.push(tempDir, exam.getUuid());
			exam.setStatus(ExamStatus.AVAILABLE);
		} catch (Exception e) {
			exam.setStatus(ExamStatus.UNAVAILABLE);
			throw e;
		} finally {
			examDAO.save(exam);
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private void releaseCode(Set<Problem> problems, File codeDir) throws IOException {
		FileUtils.cleanDirectory(codeDir);
		for (Problem problem : problems) {
			File problemDir = new File(codeDir, problem.getUniqueName());
			if (!problemDir.mkdir()) {
				throw new IOException("创建临时文件夹失败！");
			}
			byte[] content = provider.fetch(problem.getUniqueName() + ".zip");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
			ZipUtil.unpack(inputStream, problemDir);
			for (String path : problem.getPrivateFiles()) {
				if (File.separatorChar != '/') {
					path = path.replace('/', File.separatorChar);
				}
				File privateFile = new File(problemDir, path);
				if (privateFile.exists() && !privateFile.delete()) {
					log.error("删除私有文件[" + privateFile.getAbsolutePath() + "]失败！");
				}
			}
		}
	}
}
