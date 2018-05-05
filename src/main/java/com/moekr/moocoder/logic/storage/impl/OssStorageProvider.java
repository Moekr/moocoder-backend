package com.moekr.moocoder.logic.storage.impl;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.ServiceException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.moekr.moocoder.logic.storage.StorageProvider;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class OssStorageProvider implements StorageProvider {
	private OSSClient client;
	private String bucket;

	@Override
	public void initialize(Map<String, String> properties) throws IOException {
		String endpoint = properties.get("endpoint");
		String accessKeyId = properties.get("access-key-id");
		String accessKeySecret = properties.get("access-key-secret");
		String useCname = properties.get("use-cname");
		bucket = properties.get("bucket");
		Assert.notNull(endpoint, "未提供阿里云OSS的Endpoint！");
		Assert.notNull(accessKeyId, "未提供阿里云OSS的AccessKeyId！");
		Assert.notNull(accessKeySecret, "未提供阿里云OSS的AccessKeySecret！");
		Assert.notNull(bucket, "未提供阿里云OSS的Bucket！");
		CredentialsProvider provider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
		ClientConfiguration config = new ClientConfiguration();
		config.setSupportCname("true".equals(useCname));
		client = new OSSClient(endpoint, provider, config);
		try {
			Assert.isTrue(client.doesBucketExist(bucket), "Bucket [" + bucket + "] 不存在！");
		} catch (ServiceException | ClientException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void save(byte[] content, String name) throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(content.length);
		PutObjectRequest request = new PutObjectRequest(bucket, name, new ByteArrayInputStream(content), metadata);
		try {
			client.putObject(request);
		} catch (ServiceException | ClientException e) {
			throw new IOException(e);
		}
	}

	@Override
	public byte[] fetch(String name) throws IOException {
		GetObjectRequest request = new GetObjectRequest(bucket, name);
		OSSObject object;
		try {
			object = client.getObject(request);
		} catch (ServiceException e) {
			if ("NoSuchKey".equals(e.getErrorCode())) {
				throw new FileNotFoundException(name);
			}
			throw new IOException(e);
		} catch (ClientException e) {
			throw new IOException(e);
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(object.getObjectContent());
		return outputStream.toByteArray();
	}
}
