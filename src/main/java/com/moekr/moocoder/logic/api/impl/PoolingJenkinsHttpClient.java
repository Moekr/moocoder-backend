package com.moekr.moocoder.logic.api.impl;

import com.offbytwo.jenkins.client.JenkinsHttpClient;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.net.URI;

class PoolingJenkinsHttpClient extends JenkinsHttpClient {
	PoolingJenkinsHttpClient(URI uri, String username, String password) {
		super(uri, poolingHttpClient(uri, username, password));
		if (StringUtils.isNotBlank(username)) {
			HttpContext localContext = new BasicHttpContext();
			localContext.setAttribute("preemptive-auth", new BasicScheme());
			super.setLocalContext(localContext);
		}
	}

	private static CloseableHttpClient poolingHttpClient(URI uri, String username, String password) {
		HttpClientBuilder builder = addAuthentication(HttpClientBuilder.create(), uri, username, password);
		PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
		manager.setMaxTotal(256);
		manager.setDefaultMaxPerRoute(128);
		builder.setDefaultRequestConfig(RequestConfig.custom()
				.setConnectTimeout(3000)
				.setConnectionRequestTimeout(5000)
				.setSocketTimeout(10000)
				.build())
				.setConnectionManager(manager);
		return builder.build();
	}
}
