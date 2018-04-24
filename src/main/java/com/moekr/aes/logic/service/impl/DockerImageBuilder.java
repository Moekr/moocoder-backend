package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.TransactionWrapper;
import com.moekr.aes.data.dao.ProblemDAO;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.logic.api.DockerApi;
import com.moekr.aes.logic.storage.StorageProvider;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;

@Component
@CommonsLog
public class DockerImageBuilder {
	private static final String TEMP_PREFIX = "AES-DOCKER-";

	private final ProblemDAO problemDAO;
	private final TransactionWrapper wrapper;
	private final DockerApi dockerApi;
	private final StorageProvider storageProvider;


	@Autowired
	public DockerImageBuilder(ProblemDAO problemDAO, TransactionWrapper wrapper, DockerApi dockerApi, StorageProvider storageProvider) {
		this.problemDAO = problemDAO;
		this.wrapper = wrapper;
		this.dockerApi = dockerApi;
		this.storageProvider = storageProvider;
	}

	@Async
	public void asyncBuildDockerImage(Problem problem) {
		try {
			wrapper.wrap((TransactionWrapper.Method) () -> buildDockerImage(problem));
		} catch (Exception e) {
			log.error("为#" + problem.getId() + "构建Docker镜像失败[" + e.getClass() + "]: " + e.getMessage());
		}
	}

	@Transactional
	public void buildDockerImage(Problem problem) throws Exception {
		problem.setModifiedAt(LocalDateTime.now());
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			buildDockerImage(tempDir, problem);
			problemDAO.save(problem);
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private void buildDockerImage(File tempDir, Problem problem) throws Exception {
		File problemDir = new File(new File(tempDir, "code"), problem.getName());
		if (!problemDir.mkdirs()) {
			throw new IOException("创建临时文件夹失败！");
		}
		byte[] content = storageProvider.fetch(problem.getId() + ".zip");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		ZipUtil.unpack(inputStream, problemDir);
		File dockerFile = new File(tempDir, "Dockerfile");
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dockerFile)))) {
			writer.write("FROM ubuntu:16.04\n");
			writer.write("RUN apt update && apt install --no-install-recommends -y default-jre default-jdk maven\n");
			writer.write("RUN apt update && apt install --no-install-recommends -y python3-all python3-pip python3-nose\n");
			writer.write("COPY ./code /var/ws/code\n");
			writer.write("WORKDIR /var/ws/code/" + problem.getName() + "\n");
			writer.write("RUN " + problem.getType().initialCommand());
		}
		dockerApi.build(tempDir.getAbsolutePath(), problem.getImageName(), problem.getImageTag());
	}
}
