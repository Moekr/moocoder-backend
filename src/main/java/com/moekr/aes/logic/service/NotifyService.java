package com.moekr.aes.logic.service;

public interface NotifyService {
	void webHook(int id);

	void callback(int id, int buildNumber);
}
