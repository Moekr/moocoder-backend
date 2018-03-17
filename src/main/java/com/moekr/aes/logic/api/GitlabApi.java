package com.moekr.aes.logic.api;

public interface GitlabApi {
	Integer createUser(String username, String email, String password);

	Integer fetchNamespace(String username);

	String createToken(int userId);

	Integer createProject(String name);

	Integer forkProject(int userId, int projectId, int namespaceId);

	void archiveProject(int projectId);

	void createWebHook(int projectId, String webHookUrl);

	void changePassword(int userId, String password);
}
