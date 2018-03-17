package com.moekr.aes.logic.storage.impl;

import com.moekr.aes.logic.storage.StorageProvider;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class LocalStorageProvider implements StorageProvider {
	private String location;

	@Override
	public void initialize(Map<String, String> properties) throws IOException {
		location = properties.get("location");
		if (location == null) {
			throw new IllegalArgumentException("未设定文件存储位置！");
		}
		while (location.endsWith(File.separator)) {
			location = location.substring(0, location.length() - 1);
		}
		File file = new File(location);
		if (file.exists()) {
			if (!file.isDirectory()) {
				throw new IOException("存储目标不能为非文件夹！");
			}
		} else if (!file.mkdirs()) {
			throw new IOException("创建文件夹失败！");
		}
	}

	@Override
	public void save(byte[] content, String name) throws IOException {
		File file = new File(location + File.separator + name);
		FileUtils.writeByteArrayToFile(file, content);
	}

	@Override
	public byte[] fetch(String name) throws IOException {
		File file = new File(location + File.separator + name);
		return FileUtils.readFileToByteArray(file);
	}
}
