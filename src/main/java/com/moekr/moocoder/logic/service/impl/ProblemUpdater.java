package com.moekr.moocoder.logic.service.impl;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;

@CommonsLog
public class ProblemUpdater {
	private static final String TEMP_PREFIX = "AES-PROB-";
	private static final Charset CHARSET = Charset.forName("UTF-8");

	public byte[] update(byte[] origin, String path, byte[] content) throws IOException {
		if (origin == null) {
			throw new NullPointerException("原始数据为Null！");
		}
		if (content == null) {
			throw new NullPointerException("文件内容为Null！");
		}
		File tempDir = Files.createTempDirectory(TEMP_PREFIX).toFile();
		try {
			return update(tempDir, origin, path, content);
		} finally {
			FileUtils.deleteDirectory(tempDir);
		}
	}

	private byte[] update(File tempDir, byte[] origin, String path, byte[] content) throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(origin);
		ZipUtil.unpack(inputStream, tempDir, CHARSET);
		if (File.separatorChar != '/') {
			path = path.replace('/', File.separatorChar);
		}
		File file = new File(tempDir, path);
		try (OutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(content);
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipUtil.pack(tempDir, outputStream);
		return outputStream.toByteArray();
	}
}
