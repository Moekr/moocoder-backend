package com.moekr.aes.logic.api.impl;

import com.moekr.aes.logic.api.JenkinsApi;
import com.moekr.aes.logic.api.vo.BuildDetails;
import com.moekr.aes.logic.api.vo.CoberturaResult;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.AesProperties.Gitlab;
import com.moekr.aes.util.AesProperties.Jenkins;
import com.moekr.aes.util.AesProperties.Local;
import com.moekr.aes.util.AesProperties.Storage;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
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
	private String configTemplate;

	public JenkinsApiImpl(AesProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	private void initialize() throws URISyntaxException, IOException {
		Jenkins jenkins = properties.getJenkins();
		server = new JenkinsServer(new URI(jenkins.getHost()), jenkins.getUsername(), jenkins.getToken());
		configTemplate = readTemplate();
	}

	@Override
	public void createJob(int id) throws IOException {
		server.createJob(String.valueOf(id), configTemplate);
	}

	@Override
	public QueueItem invokeBuild(int id) throws IOException {
		QueueReference reference = server.getJob(String.valueOf(id)).build();
		return server.getQueueItem(reference);
	}

	@Override
	public void deleteJob(int id) throws IOException {
		server.deleteJob(String.valueOf(id));
	}

	@Override
	public BuildDetails fetchBuildDetails(int id, int buildNumber) throws IOException {
		BuildDetails buildDetails = new BuildDetails();
		BuildWithDetails build;
		build = server.getJob(String.valueOf(id)).getBuildByNumber(buildNumber).details();
		buildDetails.setConsoleOutput(build.getConsoleOutputText());
		buildDetails.setNumber(build.getNumber());
		buildDetails.setDuration(build.getDuration());
		buildDetails.setBuildResult(build.getResult());
		try {
			buildDetails.setTestResult(build.getTestResult());
		} catch (IOException e) {
			buildDetails.setTestResult(null);
		}
		try {
			buildDetails.setCoberturaResult(build.getClient().get(build.getUrl() + "/cobertura/?depth=2", CoberturaResult.class));
		} catch (IOException e) {
			buildDetails.setCoberturaResult(null);
		}
		return buildDetails;
	}

	private String readTemplate() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		InputStream inputStream = JenkinsApiImpl.class.getClassLoader().getResourceAsStream("jenkins/config.xml");
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
		template = template.replace("{%HOST%}", local.getHost());
		template = template.replace("{%SECRET%}", local.getSecret());
		return template;
	}
}
