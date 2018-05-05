package com.moekr.moocoder.logic.api;

import com.moekr.moocoder.logic.api.vo.GitlabUser;
import org.gitlab4j.api.GitLabApiException;

public interface GitlabApi {
	GitlabUser createUser(String username, String email, String password) throws GitLabApiException;

	Integer createProject(String name) throws GitLabApiException;

	Integer forkProject(int userId, int projectId, int namespaceId) throws GitLabApiException;

	void archiveProject(int projectId) throws GitLabApiException;

	void changePassword(int userId, String password) throws GitLabApiException;

	void deleteUser(int userId) throws GitLabApiException;

	void deleteProject(int projectId) throws GitLabApiException;
}
