package com.moekr.aes.logic.api;

public interface DockerApi {
	void build(String directory, String imageName, String imageTag) throws Exception;
}
