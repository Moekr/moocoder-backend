package com.moekr.aes.web.controller.internal;

import com.moekr.aes.logic.service.EnvironmentService;
import com.moekr.aes.util.AesProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/env")
public class EnvironmentController {
	private final String secret;
	private final EnvironmentService environmentService;

	@Autowired
	public EnvironmentController(AesProperties properties, EnvironmentService environmentService) {
		this.secret = properties.getLocal().getSecret();
		this.environmentService = environmentService;
	}

	@GetMapping(value = "/{id:\\d+}", params = "secret", produces = MediaType.TEXT_PLAIN_VALUE)
	public String env(@PathVariable int id, @RequestParam String secret) {
		if (!StringUtils.equals(this.secret, secret)) return "";
		Map<String, String> envMap = environmentService.env(id);
		StringBuilder builder = new StringBuilder();
		envMap.forEach((key, value) -> {
			String quotation = StringUtils.contains(value, '\n') ? "\"\"\"" : "\"";
			builder.append(key).append(" = ").append(quotation).append(value).append(quotation).append('\n');
		});
		return builder.toString();
	}
}
