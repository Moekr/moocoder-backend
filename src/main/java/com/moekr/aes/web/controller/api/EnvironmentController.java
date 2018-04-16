package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.EnvironmentService;
import com.moekr.aes.util.AesProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class EnvironmentController {
	private final String secret;
	private final EnvironmentService environmentService;

	@Autowired
	public EnvironmentController(AesProperties properties, EnvironmentService environmentService) {
		this.secret = properties.getLocal().getSecret();
		this.environmentService = environmentService;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@GetMapping(value = "/env/{resultId}", produces = MediaType.TEXT_PLAIN_VALUE)
	public String env(@PathVariable int resultId, @RequestParam Optional<String> secret) {
		if (!secret.isPresent()) return "";
		if (!StringUtils.equals(this.secret, secret.get())) return "";
		Map<String, String> envMap = environmentService.env(resultId);
		StringBuilder builder = new StringBuilder();
		envMap.forEach((key, value) -> {
			String quotation = StringUtils.contains(value, '\n') ? "\"\"\"" : "\"";
			builder.append(key).append(" = ").append(quotation).append(value).append(quotation).append('\n');
		});
		return builder.toString();
	}
}
