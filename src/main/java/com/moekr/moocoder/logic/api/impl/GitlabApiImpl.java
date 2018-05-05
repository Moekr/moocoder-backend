package com.moekr.moocoder.logic.api.impl;

import com.moekr.moocoder.logic.api.GitlabApi;
import com.moekr.moocoder.logic.api.vo.GitlabUser;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ApplicationProperties.Gitlab;
import com.moekr.moocoder.util.ToolKit;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.*;
import org.gitlab4j.api.models.ImpersonationToken.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GitlabApiImpl implements GitlabApi {
	private final Gitlab gitlab;

	private final GitLabApi server;

	@Autowired
	public GitlabApiImpl(ApplicationProperties properties) {
		gitlab = properties.getGitlab();
		this.server = new GitLabApi(gitlab.getHost(), gitlab.getToken());
	}

	@Override
	public synchronized GitlabUser createUser(String username, String email, String password) throws GitLabApiException {
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setName(username);
		user.setSkipConfirmation(true);
		user = server.getUserApi().createUser(user, password, 1000);
		GitlabUser gitlabUser = new GitlabUser();
		gitlabUser.setId(user.getId());
		gitlabUser.setNamespace(fetchNamespace(username));
		gitlabUser.setToken(createToken(user.getId()));
		return gitlabUser;
	}

	private Integer fetchNamespace(String username) throws GitLabApiException {
		Namespace namespace;
		namespace = server.getNamespaceApi().findNamespaces(username)
				.stream()
				.filter(ns -> "user".equals(ns.getKind()))
				.reduce((ns1, ns2) -> ns1.getId() > ns2.getId() ? ns1 : ns2)
				.orElse(null);
		return namespace != null ? namespace.getId() : null;
	}

	private String createToken(int userId) throws GitLabApiException {
		Scope[] scopes = new Scope[]{Scope.API};
		ImpersonationToken token;
		token = server.getUserApi().createImpersonationToken(userId, "AES-" + ToolKit.randomUUID(), null, scopes);
		return token.getToken();
	}

	@Override
	public synchronized Integer createProject(String name) throws GitLabApiException {
		Project project = new Project();
		project.setName(name);
		project.setVisibility(Visibility.INTERNAL);
		project = server.getProjectApi().createProject(project);
		return project != null ? project.getId() : null;
	}

	@Override
	public synchronized Integer forkProject(int userId, int projectId, int namespaceId) throws GitLabApiException {
		Project project;
		try {
			server.setSudoAsId(userId);
			project = server.getProjectApi().forkProject(projectId, namespaceId);
			project.setVisibility(Visibility.PRIVATE);
			project = server.getProjectApi().updateProject(project);
		} finally {
			server.unsudo();
		}
		if (project != null) {
			createWebHook(project.getId());
		}
		return project != null ? project.getId() : null;
	}

	private void createWebHook(int id) throws GitLabApiException {
		String url = gitlab.getWebHookProxy() + "/internal/notify/webhook/" + id;
		server.getProjectApi().addHook(id, url, true, false, false);
	}

	@Override
	public synchronized void archiveProject(int projectId) throws GitLabApiException {
		server.getProjectApi().archiveProject(projectId);
	}

	@Override
	public synchronized void changePassword(int userId, String password) throws GitLabApiException {
		User user;
		user = server.getUserApi().getUser(userId);
		server.getUserApi().modifyUser(user, password, 1000);
	}

	@Override
	public synchronized void deleteUser(int userId) throws GitLabApiException {
		server.getUserApi().deleteUser(userId, true);
	}

	@Override
	public void deleteProject(int projectId) throws GitLabApiException {
		server.getProjectApi().deleteProject(projectId);
	}
}
