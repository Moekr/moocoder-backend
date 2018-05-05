package com.moekr.moocoder.logic.service;

public interface NotifyService {
	void webHook(int id, String commitHash);

	void callback(int id, int buildNumber);
}
