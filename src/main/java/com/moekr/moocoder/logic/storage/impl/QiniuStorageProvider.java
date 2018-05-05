package com.moekr.moocoder.logic.storage.impl;

import com.moekr.moocoder.logic.storage.StorageProvider;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.util.Assert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QiniuStorageProvider implements StorageProvider {
	private String bucket;
	private String domain;
	private Auth auth;
	private UploadManager manager;
	private CloseableHttpClient client;

	@Override
	public void initialize(Map<String, String> properties) throws IOException {
		String accessKey = properties.get("access-key");
		String secretKey = properties.get("secret-key");
		String zone = properties.get("zone");
		String autoZone = properties.get("auto-zone");
		bucket = properties.get("bucket");
		domain = properties.get("domain");
		Assert.notNull(accessKey, "未提供七牛云存储的AccessKey！");
		Assert.notNull(secretKey, "未提供七牛云存储的SecretKey！");
		Assert.notNull(bucket, "未提供七牛云存储的Bucket！");
		Assert.notNull(domain, "未提供七牛云存储的Domain！");
		auth = Auth.create(accessKey, secretKey);
		Configuration config = new Configuration("true".equals(autoZone) ? Zone.autoZone() : zone(zone));
		BucketManager manager = new BucketManager(auth, config);
		try {
			List<String> buckets = Arrays.asList(manager.buckets());
			Assert.isTrue(!buckets.contains(bucket), "Bucket [" + bucket + "] 不存在！");
		} catch (QiniuException e) {
			throw new IOException(e);
		}
		this.manager = new UploadManager(config);
		client = HttpClients.createDefault();
	}

	@Override
	public void save(byte[] content, String name) throws IOException {
		String token = auth.uploadToken(bucket);
		try {
			Response response = manager.put(content, name, token);
			if (!response.isOK()) {
				throw new IOException(response.error);
			}
		} catch (QiniuException e) {
			throw new IOException(e);
		}
	}

	@Override
	public byte[] fetch(String name) throws IOException {
		String baseUrl = domain + "/" + URLEncoder.encode(name, "UTF-8");
		String privateUrl = auth.privateDownloadUrl(baseUrl);
		HttpGet request = new HttpGet(privateUrl);
		try (CloseableHttpResponse response = client.execute(request)) {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(response.getEntity().getContent());
				return outputStream.toByteArray();
			} else if (statusLine.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				throw new FileNotFoundException(name);
			} else {
				throw new IOException(statusLine.getReasonPhrase());
			}
		}
	}

	private Zone zone(String zone) {
		Assert.notNull(zone, "未提供七牛云存储的Zone！");
		switch (zone) {
			case "z0":
			case "hd":
				return Zone.zone0();
			case "z1":
			case "hb":
				return Zone.zone1();
			case "z2":
			case "hn":
				return Zone.zone2();
			case "na0":
			case "bm":
				return Zone.zoneNa0();
			case "as0":
			case "dny":
				return Zone.zoneAs0();
		}
		throw new IllegalArgumentException("七牛云存储的Zone信息无效！");
	}
}
