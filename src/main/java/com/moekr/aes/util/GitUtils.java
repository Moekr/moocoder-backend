package com.moekr.aes.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public abstract class GitUtils {
	public static void pushFromZipArchive(byte[] zipArchive, String target, String username, String password) throws IOException, GitAPIException {
		File unzipDir = createTempDir();
		File repoDir = createTempDir();
		CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(username, password);
		Git targetRepo = Git.cloneRepository().setURI(target).setDirectory(repoDir).setCredentialsProvider(credentialsProvider).call();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(zipArchive);
		ZipUtil.unpack(inputStream, unzipDir);
		FileUtils.copyDirectory(unzipDir, repoDir, file -> !(file.getName().equals(".git") || StringUtils.startsWithIgnoreCase(file.getName(), "private")));
		targetRepo.add().addFilepattern(".").call();
		targetRepo.commit().setMessage("初始化").call();
		targetRepo.push().setRemote("origin").setForce(true).setPushAll().setCredentialsProvider(credentialsProvider).call();
		targetRepo.close();
		deleteTempDir(unzipDir);
		deleteTempDir(repoDir);
	}

	private static File createTempDir() throws IOException {
		File tempFile = File.createTempFile("aes", "repo");
		if (!tempFile.delete()) {
			throw new IOException("Could not delete temporary file!");
		}
		if (!tempFile.mkdir()) {
			throw new IOException("Could not create temporary dir!");
		}
		return tempFile;
	}

	private static void deleteTempDir(File file) throws IOException {
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			for (File chileFile : childFiles != null ? childFiles : new File[0]) {
				deleteTempDir(chileFile);
			}
		}
		if (!file.delete()) {
			throw new IOException("Could not delete temporary file!");
		}
	}
}
