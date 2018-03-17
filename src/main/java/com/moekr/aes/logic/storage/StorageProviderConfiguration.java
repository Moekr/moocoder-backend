package com.moekr.aes.logic.storage;

import com.moekr.aes.logic.storage.impl.LocalStorageProvider;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.AesProperties.Storage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class StorageProviderConfiguration {
	private final AesProperties properties;

	public StorageProviderConfiguration(AesProperties properties) {
		this.properties = properties;
	}

	@Bean
	public StorageProvider storageProvider() throws IOException {
		Storage storage = properties.getStorage();
		StorageProvider storageProvider;
		switch (storage.getType()) {
			case "local":
				storageProvider = new LocalStorageProvider();
				break;
			default:
				throw new IllegalArgumentException("没有类型为[" + storage.getType() + "]的文件存储实现！");
		}
		storageProvider.initialize(storage.getProperties());
		return storageProvider;
	}
}
