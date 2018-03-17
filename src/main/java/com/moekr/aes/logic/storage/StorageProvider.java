package com.moekr.aes.logic.storage;

import java.io.IOException;
import java.util.Map;

public interface StorageProvider {
	void initialize(Map<String, String> properties) throws IOException;

	void save(byte[] content, String name) throws IOException;

	byte[] fetch(String name) throws IOException;
}
