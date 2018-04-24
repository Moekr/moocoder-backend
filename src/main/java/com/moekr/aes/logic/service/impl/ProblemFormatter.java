package com.moekr.aes.logic.service.impl;

import com.moekr.aes.util.enums.FileType;
import com.moekr.aes.util.enums.ProblemType;
import com.moekr.aes.util.exceptions.MalformedProblemArchiveException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.util.exceptions.UnsupportedProblemTypeException;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipException;

@CommonsLog
public class ProblemFormatter {
	private static final String TEMP_PREFIX = "AES-PROB-";
	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9-_]+$");
	private static final Charset CHARSET = Charset.forName("UTF-8");
	private static final Charset ALT_CHARSET = Charset.forName("GBK");
	private static final int README_MIN_LINE = 4;
	private static final int README_LANG_INDEX = 0;
	private static final int README_NAME_INDEX = 1;
	private static final int README_BLANK_INDEX = 2;
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

	public FormattedProblemInfo format(byte[] content) throws IOException, ServiceException {
		if (content == null) {
			throw new NullPointerException("待格式化的内容为Null！");
		}
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			return format(content, tempDir);
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private FormattedProblemInfo format(byte[] content, File tempDir) throws IOException, ServiceException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
		try {
			ZipUtil.unpack(inputStream, tempDir, CHARSET);
		} catch (RuntimeException e1) {
			log.debug("尝试使用UTF-8编码解压缩文件失败，切换至GBK编码[" + e1.getClass() + "]: " + e1.getMessage());
			FileUtils.cleanDirectory(tempDir);
			inputStream = new ByteArrayInputStream(content);
			try {
				ZipUtil.unpack(inputStream, tempDir, ALT_CHARSET);
			} catch (RuntimeException e2) {
				log.debug("尝试使用GBK编码解压缩文件失败，文件可能损坏[" + e2.getClass() + "]: " + e2.getMessage());
				throw new ZipException("解压缩文件失败，文件可能损坏！");
			}
		}
		FormattedProblemInfo info = new FormattedProblemInfo();
		File readme = new File(tempDir, "README.md");
		if (!readme.exists()) {
			throw new MalformedProblemArchiveException("找不到题目描述文件（README.md）！");
		}
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(readme), CHARSET))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		if (lines.size() < README_MIN_LINE || !lines.get(README_BLANK_INDEX).isEmpty()) {
			throw new MalformedProblemArchiveException("无法识别的题目描述文件！");
		}
		try {
			info.setType(ProblemType.valueOf(lines.get(README_LANG_INDEX).trim().toUpperCase()));
		} catch (IllegalArgumentException e) {
			throw new UnsupportedProblemTypeException(lines.get(README_LANG_INDEX).trim().toUpperCase());
		}
		String name = lines.get(README_NAME_INDEX).trim();
		if (name.isEmpty()) {
			throw new MalformedProblemArchiveException("题目名称为空！");
		}
		if (!NAME_PATTERN.matcher(name).matches()) {
			throw new MalformedProblemArchiveException("题目名称含有非法字符！");
		}
		info.setName(name);
		StringBuilder builder = new StringBuilder();
		lines.stream().skip(README_MIN_LINE - 1).forEach(line -> builder.append(line).append('\n'));
		info.setDescription(builder.toString());
		deleteUselessFiles(tempDir);
		List<String> fileList = listFiles(tempDir);
		for (String file : fileList) {
			FileType fileType = info.getType().getHelper().fileType(file);
			if (fileType == FileType.PUBLIC) {
				info.getPublicFiles().add(file);
			} else if (fileType == FileType.PROTECTED) {
				info.getProtectedFiles().add(file);
			} else if (fileType == FileType.PRIVATE) {
				info.getPrivateFiles().add(file);
			}
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipUtil.pack(tempDir, outputStream);
		info.setFormattedContent(outputStream.toByteArray());
		return info;
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
