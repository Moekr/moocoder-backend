package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.dao.ProblemDAO;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.logic.api.DockerApi;
import com.moekr.aes.logic.storage.StorageProvider;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
	private final DockerApi dockerApi;
	private final StorageProvider storageProvider;


	@Autowired
	public DockerImageBuilder(ProblemDAO problemDAO, DockerApi dockerApi, StorageProvider storageProvider) {
		this.problemDAO = problemDAO;
		this.dockerApi = dockerApi;
		this.storageProvider = storageProvider;
	}

	@Transactional(rollbackFor = Exception.class)
	public void buildDockerImage(int problemId) throws Exception {
		Problem problem = problemDAO.findById(problemId);
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
		File problemDir = new File(new File(tempDir, "code"), problem.getUniqueName());
		if (!problemDir.mkdirs()) {
			throw new IOException("创建临时文件夹失败！");
		}
		byte[] content = storageProvider.fetch(problem.getUniqueName() + ".zip");
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		ZipUtil.unpack(inputStream, problemDir);
		File dockerFile = new File(tempDir, "Dockerfile");
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dockerFile)))) {
			writer.write(problem.getType().getHelper().dockerFile(problem.getUniqueName()));
		}
		dockerApi.build(tempDir.getAbsolutePath(), problem.getImageName(), problem.getImageTag());
	}
}
