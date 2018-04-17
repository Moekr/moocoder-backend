package com.moekr.aes.logic.service.impl;

import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.logic.storage.StorageProvider;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

@Component
@CommonsLog
public class PaperBuilder {
	private static final String TEMP_PREFIX = "AES-CODE-";

	private final StorageProvider provider;
	private final GitProcessHelper helper;

	@Autowired
	public PaperBuilder(StorageProvider provider, GitProcessHelper helper) {
		this.provider = provider;
		this.helper = helper;
	}

	public void buildPaper(Examination examination) throws IOException, GitAPIException {
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			releaseCode(examination.getProblemSet(), tempDir);
			helper.push(tempDir, examination.getUuid());
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	public void releaseCode(Set<Problem> problemSet, File codeDir) throws IOException {
		FileUtils.cleanDirectory(codeDir);
		for (Problem problem : problemSet) {
			File problemDir = new File(codeDir, problem.getName());
			if (!problemDir.mkdir()) {
				throw new IOException("创建临时文件夹失败！");
			}
			byte[] content = provider.fetch(problem.getId() + ".zip");
			ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
			ZipUtil.unpack(inputStream, problemDir);
		}
	}
}
