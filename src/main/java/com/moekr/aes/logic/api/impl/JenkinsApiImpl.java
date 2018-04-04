package com.moekr.aes.logic.api.impl;

import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.api.impl.cobertura.CoberturaResult;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.AesProperties.Gitlab;
import com.moekr.aes.util.AesProperties.Jenkins;
import com.moekr.aes.util.AesProperties.Local;
import com.moekr.aes.util.AesProperties.Storage;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.enums.Language;
import com.moekr.aes.util.enums.Role;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.TestResult;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class JenkinsApiImpl implements JenkinsApi {
	private final AesProperties properties;

	private JenkinsServer server;
	private String javaTemplate;
	private String pythonTemplate;

	public JenkinsApiImpl(AesProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	private void initialize() throws URISyntaxException, IOException {
		Jenkins jenkins = properties.getJenkins();
		server = new JenkinsServer(new URI(jenkins.getHost()), jenkins.getUsername(), jenkins.getToken());
		javaTemplate = readTemplate("jenkins/java.xml");
		pythonTemplate = readTemplate("jenkins/python.xml");
	}

	@Override
	public String createJob(int id, String namespace, String project, String problem, Language language, Role role) {
		String config;
		switch (language) {
			case JAVA:
				config = javaTemplate;
				break;
			case PYTHON:
				config = pythonTemplate;
				break;
			default:
				throw new IllegalArgumentException("不支持的编程语言！");
		}
		config = config.replace("{%ID%}", String.valueOf(id));
		config = config.replace("{%NAMESPACE%}", namespace);
		config = config.replace("{%PROJECT%}", project);
		config = config.replace("{%PROBLEM%}", problem);
		// 教师自测在主机运行，同时兼有安装测试所需依赖的目的；学生测试在无网络的Docker内运行
		config = config.replace("{%TEACHER_MASK%}", role == Role.TEACHER ? "//" : "");
		try {
			server.createJob(String.valueOf(id), config);
		} catch (IOException e) {
			throw new ServiceException("创建Jenkins项目失败！");
		}
		Jenkins jenkins = properties.getJenkins();
		String host = jenkins.getHost().replace("//", "//" + jenkins.getUsername() + ":" + jenkins.getToken() + "@");
		return host + "/project/" + String.valueOf(id);
	}

	@Override
	public void deleteJob(String name) {
		try {
			server.deleteJob(name);
		} catch (IOException e) {
			throw new ServiceException("删除Jenkins项目失败！");
		}
	}

	@Override
	public TestResult fetchTestResult(int id, int buildNumber) {
		try {
			return server.getJob(String.valueOf(id)).getBuildByNumber(buildNumber).getTestResult();
		} catch (IOException e) {
			throw new ServiceException("获取项目测试结果失败！");
		}
	}

	@Override
	public CoberturaResult fetchCoberturaResult(int id, int buildNumber) {
		try {
			Build build = server.getJob(String.valueOf(id)).getBuildByNumber(buildNumber);
			return build.getClient().get(build.getUrl() + "/cobertura/?depth=2", CoberturaResult.class);
		} catch (IOException e) {
			throw new ServiceException("获取测试覆盖结果失败！");
		}
	}

	private String readTemplate(String templateFile) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		InputStream inputStream = JenkinsApiImpl.class.getClassLoader().getResourceAsStream(templateFile);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String buffer;
		while ((buffer = bufferedReader.readLine()) != null) {
			stringBuilder.append(buffer).append("\n");
		}
		String template = stringBuilder.toString();
		Gitlab gitlab = properties.getGitlab();
		Jenkins jenkins = properties.getJenkins();
		Storage storage = properties.getStorage();
		Local local = properties.getLocal();
		template = template.replace("{%GIT_HOST%}", gitlab.getHost());
		template = template.replace("{%CREDENTIAL%}", jenkins.getCredential());
		template = template.replace("{%STORAGE_HOST%}", storage.getHost());
		template = template.replace("{%LOCAL_HOST%}", local.getHost());
		template = template.replace("{%SECRET%}", local.getSecret());
		return template;
	}
}
