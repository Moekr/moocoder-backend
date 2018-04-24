package com.moekr.aes.logic.service.impl;

import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.AesProperties.Gitlab;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class GitHelper {
	private static final String TEMP_PREFIX = "AES-REPO-";
	private static final String COMMIT_MESSAGE = "初始化";

	private final Gitlab gitlab;
	private final CredentialsProvider provider;

	@Autowired
	public GitHelper(AesProperties properties) {
		this.gitlab = properties.getGitlab();
		this.provider = new UsernamePasswordCredentialsProvider(gitlab.getUsername(), gitlab.getToken());
	}

	public void push(File sourceDir, String project) throws GitAPIException, IOException {
		String uri = gitlab.getHost() + "/" + gitlab.getUsername() + "/" + project + ".git";
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			push(sourceDir, tempDir, uri);
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private void push(File sourceDir, File tempDir, String uri) throws GitAPIException, IOException {
		Git targetRepo = Git.cloneRepository().setURI(uri).setDirectory(tempDir).setCredentialsProvider(provider).call();
		FileUtils.copyDirectory(sourceDir, tempDir);
		targetRepo.add().addFilepattern(".").call();
		targetRepo.commit().setMessage(COMMIT_MESSAGE).call();
		targetRepo.push().setRemote("origin").setForce(true).setPushAll().setCredentialsProvider(provider).call();
		targetRepo.close();
	}
}
