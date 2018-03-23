package com.moekr.aes.logic.api.impl;

import com.moekr.aes.logic.api.GitlabApi;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.AesProperties.Gitlab;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.ToolKit;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.*;
import org.gitlab4j.api.models.ImpersonationToken.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitlabApiImpl implements GitlabApi {
	private final GitLabApi server;

	@Autowired
	public GitlabApiImpl(AesProperties properties) {
		Gitlab gitlab = properties.getGitlab();
		this.server = new GitLabApi(gitlab.getHost(), gitlab.getToken());
	}

	@Override
	public synchronized Integer createUser(String username, String email, String password) {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setName(username);
		user.setSkipConfirmation(true);
		try {
			user = server.getUserApi().createUser(user, password, 1000);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab用户失败！");
		}
		return user.getId();
	}

	@Override
	public synchronized Integer fetchNamespace(String username) {
		Namespace namespace;
		try {
			namespace = server.getNamespaceApi().findNamespaces(username)
					.stream()
					.filter(ns -> "user".equals(ns.getKind()))
					.reduce((ns1, ns2) -> ns1.getId() > ns2.getId() ? ns1 : ns2)
					.orElse(null);
		} catch (GitLabApiException e) {
			throw new ServiceException("获取用户命名空间失败！");
		}
		return namespace != null ? namespace.getId() : null;
	}

	@Override
	public synchronized String createToken(int userId) {
		Scope[] scopes = new Scope[]{Scope.API};
		ImpersonationToken token;
		try {
			token = server.getUserApi().createImpersonationToken(userId, "AES-" + ToolKit.randomUUID(), null, scopes);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建用户API Token失败！");
		}
		return token.getToken();
	}

	@Override
	public synchronized Integer createProject(String name) {
		Project project = new Project();
		project.setName(name);
		project.setVisibility(Visibility.INTERNAL);
		try {
			project = server.getProjectApi().createProject(project);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建GitLab项目失败！");
		}
		return project != null ? project.getId() : null;
	}

	@Override
	public synchronized Integer forkProject(int userId, int projectId, int namespaceId) {
		Project project;
		try {
			server.setSudoAsId(userId);
			project = server.getProjectApi().forkProject(projectId, namespaceId);
			project.setVisibility(Visibility.PRIVATE);
			project = server.getProjectApi().updateProject(project);
		} catch (GitLabApiException e) {
			throw new ServiceException("Fork项目失败！");
		} finally {
			server.unsudo();
		}
		return project != null ? project.getId() : null;
	}

	@Override
	public synchronized void archiveProject(int projectId) {
		try {
			server.getProjectApi().archiveProject(projectId);
		} catch (GitLabApiException e) {
			throw new ServiceException("设置项目归档失败！");
		}
	}

	@Override
	public synchronized void createWebHook(int projectId, String webHookUrl) {
		try {
			server.getProjectApi().addHook(projectId, webHookUrl, true, false, false);
		} catch (GitLabApiException e) {
			throw new ServiceException("创建WebHook失败！");
		}
	}

	@Override
	public synchronized void changePassword(int userId, String password) {
		User user;
		try {
			user = server.getUserApi().getUser(userId);
		} catch (GitLabApiException e) {
			throw new ServiceException("拉取GitLab用户信息失败！");
		}
		try {
			server.getUserApi().modifyUser(user, password, 1000);
		} catch (GitLabApiException e) {
			throw new ServiceException("修改GitLab密码失败！");
		}
	}

	@Override
	public synchronized void deleteUser(int userId) {
		try {
			server.getUserApi().deleteUser(userId, true);
		} catch (GitLabApiException e) {
			throw new ServiceException("删除GitLab用户失败！");
		}
	}

	@Override
	public void deleteProject(int projectId) {
		try {
			server.getProjectApi().deleteProject(projectId);
		} catch (GitLabApiException e) {
			throw new ServiceException("删除GitLab项目失败！");
		}
	}
}
