package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.TransactionWrapper;
import com.moekr.aes.data.dao.ExaminationDAO;
import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.logic.api.DockerApi;
import com.moekr.aes.util.enums.ExaminationStatus;
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

	private final ExaminationDAO examinationDAO;
	private final TransactionWrapper wrapper;
	private final PaperBuilder builder;
	private final DockerApi dockerApi;

	@Autowired
	public DockerImageBuilder(ExaminationDAO examinationDAO, TransactionWrapper wrapper, PaperBuilder builder, DockerApi dockerApi) {
		this.examinationDAO = examinationDAO;
		this.wrapper = wrapper;
		this.builder = builder;
		this.dockerApi = dockerApi;
	}

	@Async
	public void asyncBuildDockerImage(Examination examination) {
		try {
			wrapper.wrap((TransactionWrapper.Method) () -> buildDockerImage(examination));
		} catch (Exception e) {
			log.error("为#" + examination.getId() + "构建Docker镜像失败[" + e.getClass() + "]: " + e.getMessage());
		}
	}

	private void buildDockerImage(Examination examination) throws Exception {
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			buildDockerImage(tempDir, examination);
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private void buildDockerImage(File tempDir, Examination examination) throws Exception {
		File dockerFile = new File(tempDir, "Dockerfile");
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dockerFile)))) {
			writer.write("FROM ubuntu:16.04\n");
			writer.write("RUN apt update && apt install --no-install-recommends -y default-jre default-jdk maven\n");
			writer.write("RUN apt update && apt install --no-install-recommends -y python3-all python3-pip python3-nose\n");
			writer.write("COPY ./code /var/ws/code\n");
			for (Problem problem : examination.getProblemSet()) {
				writer.write("WORKDIR /var/ws/code/" + problem.getName() + "\n");
				writer.write("RUN " + problem.getType().initialCommand());
			}
		}
		File codeDir = new File(tempDir, "code");
		if (!codeDir.mkdir()) {
			throw new IOException("创建临时文件夹失败！");
		}
		builder.releaseCode(examination.getProblemSet(), codeDir);
		int version = examination.getVersion() + 1;
		dockerApi.build(tempDir.getAbsolutePath(), examination.getUuid(), String.valueOf(version));
		examination.setVersion(version);
		if (examination.getStatus() == ExaminationStatus.PREPARING) {
			examination.setStatus(ExaminationStatus.AVAILABLE);
		}
		examinationDAO.save(examination);
	}
}
