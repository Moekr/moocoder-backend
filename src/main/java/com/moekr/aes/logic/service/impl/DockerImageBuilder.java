package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.TransactionWrapper;
import com.moekr.aes.data.dao.ExamDAO;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.logic.api.DockerApi;
import com.moekr.aes.util.enums.ExamStatus;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;

@Component
@CommonsLog
public class DockerImageBuilder {
	private static final String TEMP_PREFIX = "AES-DOCKER-";

	private final ExamDAO examDAO;
	private final TransactionWrapper wrapper;
	private final ExamPaperBuilder builder;
	private final DockerApi dockerApi;

	@Autowired
	public DockerImageBuilder(ExamDAO examDAO, TransactionWrapper wrapper, ExamPaperBuilder builder, DockerApi dockerApi) {
		this.examDAO = examDAO;
		this.wrapper = wrapper;
		this.builder = builder;
		this.dockerApi = dockerApi;
	}

	@Async
	public void asyncBuildDockerImage(Exam exam) {
		try {
			wrapper.wrap((TransactionWrapper.Method) () -> buildDockerImage(exam));
		} catch (Exception e) {
			log.error("为#" + exam.getId() + "构建Docker镜像失败[" + e.getClass() + "]: " + e.getMessage());
		}
	}

	private void buildDockerImage(Exam exam) throws Exception {
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			buildDockerImage(tempDir, exam);
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private void buildDockerImage(File tempDir, Exam exam) throws Exception {File codeDir = new File(tempDir, "code");
		if (!codeDir.mkdir()) {
			throw new IOException("创建临时文件夹失败！");
		}
		builder.releaseCode(exam.getProblemSet(), codeDir, false);
		File dockerFile = new File(tempDir, "Dockerfile");
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dockerFile)))) {
			writer.write("FROM ubuntu:16.04\n");
			writer.write("RUN apt update && apt install --no-install-recommends -y default-jre default-jdk maven\n");
			writer.write("RUN apt update && apt install --no-install-recommends -y python3-all python3-pip python3-nose\n");
			writer.write("COPY ./code /var/ws/code\n");
			for (Problem problem : exam.getProblemSet()) {
				writer.write("WORKDIR /var/ws/code/" + problem.getName() + "\n");
				writer.write("RUN " + problem.getType().initialCommand());
			}
		}
		int version = exam.getVersion() + 1;
		dockerApi.build(tempDir.getAbsolutePath(), exam.getUuid(), String.valueOf(version));
		exam.setVersion(version);
		if (exam.getStatus() == ExamStatus.PREPARING) {
			exam.setStatus(ExamStatus.AVAILABLE);
		}
		examDAO.save(exam);
	}
}
