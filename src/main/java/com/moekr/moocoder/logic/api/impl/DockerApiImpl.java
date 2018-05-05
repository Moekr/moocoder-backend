package com.moekr.moocoder.logic.api.impl;

import com.moekr.moocoder.logic.api.DockerApi;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ApplicationProperties.Docker;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ProgressMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class DockerApiImpl implements DockerApi {
	private final String registry;
	private final DockerClient client;
	private final ProgressHandler handler;

	@Autowired
	public DockerApiImpl(ApplicationProperties properties) {
		Docker docker = properties.getDocker();
		registry = docker.getRegistry();
		client = new DefaultDockerClient(docker.getHost());
		handler = new NoActionProgressHandler();
	}

	public void build(String directory, String imageName, String imageTag) throws Exception {
		String imageId = client.build(new File(directory).toPath(), handler, BuildParam.name(registry + "/" + imageName + ":" + imageTag), BuildParam.forceRm());
		if (imageId == null) {
			throw new IOException("Failed to build docker image under [" + directory + "]");
		}
		client.push(registry + "/" + imageName + ":" + imageTag, handler);
	}

	private class NoActionProgressHandler implements ProgressHandler {
		@Override
		public void progress(ProgressMessage message) {
			//IGNORE THE MESSAGE
		}
	}
}
