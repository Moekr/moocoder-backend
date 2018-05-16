package com.moekr.moocoder.logic.api.impl;

import com.moekr.moocoder.logic.api.JenkinsApi;
import com.moekr.moocoder.logic.api.vo.BuildDetails;
import com.moekr.moocoder.logic.api.vo.CoverageResult;
import com.moekr.moocoder.logic.api.vo.MutationResult;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.ApplicationProperties.Jenkins;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.Artifact;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.QueueItem;
import com.offbytwo.jenkins.model.QueueReference;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.dom4j.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Component
@CommonsLog
public class JenkinsApiImpl implements JenkinsApi {
	private final ApplicationProperties properties;

	private JenkinsServer server;
	private String configTemplate;

	public JenkinsApiImpl(ApplicationProperties properties) {
		this.properties = properties;
	}

	@PostConstruct
	private void initialize() throws URISyntaxException, IOException {
		Jenkins jenkins = properties.getJenkins();
		URI uri = new URI(jenkins.getHost());
		String username = jenkins.getUsername();
		String token = jenkins.getToken();
		JenkinsHttpClient client = new PoolingJenkinsHttpClient(uri, username, token);
		server = new JenkinsServer(client);
		configTemplate = readTemplate();
	}

	@Override
	public void createJob(int id) throws IOException {
		server.createJob(String.valueOf(id), configTemplate);
	}

	@Override
	public QueueItem invokeBuild(int id, Map<String, String> paramMap) throws IOException {
		QueueReference reference;
		if (paramMap == null) {
			reference = server.getJob(String.valueOf(id)).build();
		} else {
			// 0.3.7版本的Jenkins客户端库中build(Map<String, String> params)方法存在BUG，会触发两次构建
			reference = server.getJob(String.valueOf(id)).build(paramMap, false);
		}
		QueueItem item =  server.getQueueItem(reference);
		while (item.getExecutable() == null) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			item =  server.getQueueItem(reference);
		}
		return item;
	}

	@Override
	public void deleteJob(int id) throws IOException {
		server.deleteJob(String.valueOf(id));
	}

	@Override
	public BuildDetails fetchBuildDetails(int id, int buildNumber, String target) throws IOException {
		BuildDetails result = new BuildDetails();
		BuildWithDetails build;
		build = server.getJob(String.valueOf(id)).getBuildByNumber(buildNumber).details();
		while (build.getResult() == null || build.getDuration() == 0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			build = server.getJob(String.valueOf(id)).getBuildByNumber(buildNumber).details();
		}
		result.setConsoleOutput(build.getConsoleOutputText());
		result.setNumber(build.getNumber());
		result.setDuration(build.getDuration());
		result.setBuildResult(build.getResult());
		fetchTargetResult(result, build, target);
		return result;
	}

	private void fetchTargetResult(BuildDetails result, BuildWithDetails build, String target) {
		try {
			if ("TEST".equals(target)) {
				fetchTestResult(result, build);
			} else if ("COVERAGE".equals(target)) {
				fetchCoverageResult(result, build);
			} else if ("MUTATION".equals(target)) {
				fetchMutationResult(result, build);
			}
		} catch (Exception ignore) { }
	}

	private void fetchTestResult(BuildDetails result, BuildWithDetails build) throws IOException {
		result.setTestResult(build.getTestResult());
	}

	private void fetchCoverageResult(BuildDetails result, BuildWithDetails build) throws IOException {
		result.setCoverageResult(build.getClient().get(build.getUrl() + "/cobertura/?depth=2", CoverageResult.class));
	}

	private void fetchMutationResult(BuildDetails result, BuildWithDetails build) throws IOException, URISyntaxException, DocumentException {
		List<Artifact> artifactList = build.getArtifacts();
		Artifact artifact = artifactList.stream().filter(a -> a.getFileName().equals("mutations.xml")).findFirst().orElse(null);
		if (artifact != null) {
			MutationResult mutationResult = new MutationResult();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(build.downloadArtifact(artifact));
			String mutationReport = new String(outputStream.toByteArray());
			Document document = DocumentHelper.parseText(mutationReport);
			Element root = document.getRootElement();
			for (Object element : root.elements("mutation")) {
				if (element instanceof Element) {
					Attribute attribute = ((Element) element).attribute("detected");
					if (attribute != null) {
						mutationResult.setMutations(mutationResult.getMutations() + 1);
						if ("true".equals(attribute.getValue())) {
							mutationResult.setDetectedMutations(mutationResult.getDetectedMutations() + 1);
						}
					}
				}
			}
			result.setMutationResult(mutationResult);
		}
	}

	private String readTemplate() throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		InputStream inputStream = JenkinsApiImpl.class.getClassLoader().getResourceAsStream("jenkins/config.xml");
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String buffer;
		while ((buffer = bufferedReader.readLine()) != null) {
			stringBuilder.append(buffer).append("\n");
		}
		return stringBuilder.toString();
	}
}
