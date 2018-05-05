package com.moekr.moocoder.logic.api;

public interface DockerApi {
	void build(String directory, String imageName, String imageTag) throws Exception;
}
