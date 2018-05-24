package com.moekr.moocoder.logic.service.impl;

import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.util.ToolKit;
import com.moekr.moocoder.util.enums.FileType;
import com.moekr.moocoder.util.exceptions.MalformedProblemException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.util.problem.helper.ProblemHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

@CommonsLog
public class ProblemFormatter {
	private static final String TEMP_PREFIX = "MOOCODER-PROBLEM-";
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final Charset ALT_CHARSET = Charset.forName("GBK");
	private static final List<Pattern> USELESS_FILE_PATH_PATTERN = Arrays.asList(
			Pattern.compile("^/target/"),
			Pattern.compile("^/bin/"),
			Pattern.compile("^/out/")
	);
	private static final List<Pattern> USELESS_FILE_NAME_PATTERN = Arrays.asList(
			Pattern.compile("^\\.(DS_Store|git|project|classpath|idea)$"),
			Pattern.compile("^(Desktop\\.ini|Thumbs\\.db|__pycache__)$"),
			Pattern.compile("\\.(iml|py[cod])$")
	);

	public byte[] format(Problem problem, byte[] content) throws IOException, ServiceException {
		if (content == null) {
			throw new NullPointerException("待格式化的内容为Null！");
		}
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			return format(problem, content, tempDir);
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private byte[] format(Problem problem, byte[] content, File tempDir) throws IOException, ServiceException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		try {
			ZipUtil.unpack(inputStream, tempDir, CHARSET);
		} catch (RuntimeException e1) {
			log.debug("尝试使用UTF-8编码解压缩文件失败，切换至GBK编码" + ToolKit.format(e1));
			FileUtils.cleanDirectory(tempDir);
			inputStream = new ByteArrayInputStream(content);
			try {
				ZipUtil.unpack(inputStream, tempDir, ALT_CHARSET);
			} catch (RuntimeException e2) {
				log.debug("尝试使用GBK编码解压缩文件失败，文件可能损坏" + ToolKit.format(e2));
				throw new ZipException("解压缩文件失败，文件可能损坏！");
			}
		}
		deleteUselessFiles(tempDir);
		ProblemHelper helper = problem.getType().getHelper();
		List<String> fileList = listFiles(tempDir);
		if (!helper.validate(fileList)) {
			throw new MalformedProblemException("题目文件未能通过模式校验");
		}
		for (String file : fileList) {
			FileType fileType = helper.fileType(file);
			if (fileType == FileType.PUBLIC) {
				problem.getPublicFiles().add(file);
			} else if (fileType == FileType.PROTECTED) {
				problem.getProtectedFiles().add(file);
			} else if (fileType == FileType.PRIVATE) {
				problem.getPrivateFiles().add(file);
			}
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipUtil.pack(tempDir, outputStream);
		return outputStream.toByteArray();
	}

	private void deleteUselessFiles(File directory) throws IOException {
		deleteUselessFiles(directory, directory);
	}

	private void deleteUselessFiles(File rootDir, File currentDir) throws IOException {
		File[] uselessFiles = currentDir.listFiles((file) -> isUselessFile(rootDir, file));
		if (uselessFiles == null) {
			throw new IOException("枚举文件时发生异常！");
		}
		for (File uselessFile : uselessFiles) {
			FileUtils.forceDelete(uselessFile);
		}
		File[] childrenDir = currentDir.listFiles(File::isDirectory);
		if (childrenDir == null) {
			throw new IOException("枚举文件时发生异常！");
		}
		for (File childDir : childrenDir) {
			deleteUselessFiles(rootDir, childDir);
		}
	}

	private boolean isUselessFile(File rootDir, File file) {
		for (Pattern pattern : USELESS_FILE_NAME_PATTERN) {
			if (pattern.matcher(file.getName()).matches()) {
				return true;
			}
		}
		String path = file.getAbsolutePath().substring(rootDir.getAbsolutePath().length());
		if (file.isDirectory()) {
			path = path + File.separator;
		}
		for (Pattern pattern : USELESS_FILE_PATH_PATTERN) {
			if (pattern.matcher(path).matches()) {
				return true;
			}
		}
		return false;
	}

	private List<String> listFiles(File directory) throws IOException {
		List<String> list = new ArrayList<>();
		listFiles(directory, directory, list);
		return list;
	}

	private void listFiles(File rootDir, File currentDir, List<String> list) throws IOException {
		File[] files = currentDir.listFiles();
		if (files == null) {
			throw new IOException("枚举文件时发生异常！");
		}
		for (File file : files) {
			if (file.isDirectory()) {
				listFiles(rootDir, file, list);
			} else {
				String filePath = file.getAbsolutePath().substring(rootDir.getAbsolutePath().length());
				if (File.separatorChar != '/') {
					filePath = filePath.replace(File.separatorChar, '/');
				}
				list.add(filePath);
			}
		}
	}
}
