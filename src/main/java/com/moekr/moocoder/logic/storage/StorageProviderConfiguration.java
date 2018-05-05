package com.moekr.moocoder.logic.storage;

import com.moekr.moocoder.logic.storage.impl.CosStorageProvider;
import com.moekr.moocoder.logic.storage.impl.LocalStorageProvider;
import com.moekr.moocoder.logic.storage.impl.OssStorageProvider;
import com.moekr.moocoder.logic.storage.impl.QiniuStorageProvider;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ApplicationProperties.Storage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class StorageProviderConfiguration {
	private final ApplicationProperties properties;

	public StorageProviderConfiguration(ApplicationProperties properties) {
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
			case "oss":
				storageProvider = new OssStorageProvider();
				break;
			case "cos":
				storageProvider = new CosStorageProvider();
				break;
			case "qiniu":
				storageProvider = new QiniuStorageProvider();
				break;
			default:
				throw new IllegalArgumentException("没有类型为[" + storage.getType() + "]的文件存储实现！");
		}
		storageProvider.initialize(storage.getProperties());
		return storageProvider;
	}
}
