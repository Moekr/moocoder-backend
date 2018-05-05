package com.moekr.moocoder.logic.storage.impl;

import com.moekr.moocoder.logic.storage.StorageProvider;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpStatus;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class CosStorageProvider implements StorageProvider {
	private COSClient client;
	private String bucket;

	@Override
	public void initialize(Map<String, String> properties) throws IOException {
		String accessKey = properties.get("access-key");
		String secretKey = properties.get("secret-key");
		String region = properties.get("region");
		bucket = properties.get("bucket");
		Assert.notNull(accessKey, "未提供腾讯云COS的AccessKey！");
		Assert.notNull(secretKey, "未提供腾讯云COS的SecretKey！");
		Assert.notNull(region, "未提供腾讯云COS的Region！");
		Assert.notNull(bucket, "未提供腾讯云COS的Bucket！");
		COSCredentials credentials = new BasicCOSCredentials(accessKey, secretKey);
		ClientConfig config = new ClientConfig(new Region(region));
		client = new COSClient(credentials, config);
		try {
			Assert.isTrue(client.doesBucketExist(bucket), "Bucket [" + bucket + "] 不存在！");
		} catch (CosClientException e) {
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
		} catch (CosClientException e) {
			throw new IOException(e);
		}
	}

	@Override
	public byte[] fetch(String name) throws IOException {
		GetObjectRequest request = new GetObjectRequest(bucket, name);
		COSObject object;
		try {
			object = client.getObject(request);
		} catch (CosServiceException e) {
			if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				throw new FileNotFoundException(name);
			}
			throw new IOException(e);
		} catch (CosClientException e) {
			throw new IOException(e);
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(object.getObjectContent());
		return outputStream.toByteArray();
	}
}
