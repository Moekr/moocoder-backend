package com.moekr.aes.util;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties("aes")
@Validated
public class AesProperties {
	@Pattern(regexp = "^[a-zA-Z0-9]+$")
	@NotEmpty
	private String secret;
	@Valid
	private Gitlab gitlab = new Gitlab();
	@Valid
	private Jenkins jenkins = new Jenkins();
	@Valid
	private Docker docker = new Docker();
	@Valid
	private Mail mail = new Mail();
	@Valid
	private Storage storage = new Storage();

	@Data
	public static class Gitlab {
		@URL
		@NotEmpty
		private String host;
		private String proxy;
		@NotEmpty
		private String username = "root";
		@NotEmpty
		private String token;

		public String getProxy() {
			return StringUtils.defaultString(proxy, host);
		}
	}

	@Data
	public static class Jenkins {
		@URL
		@NotEmpty
		private String host;
		@NotEmpty
		private String username = "root";
		@NotEmpty
		private String token;
	}

	@Data
	public static class Docker {
		@URL
		@NotEmpty
		private String host = "unix:///var/run/docker.sock";
		@NotEmpty
		private String registry;
	}

	@Data
	public static class Mail {
		@Email
		@NotEmpty
		private String from;
		@NotEmpty
		private String personal = "Automated Examination System";
	}

	@Data
	public static class Storage {
		private String host = "";
		private String type = "local";
		private Map<String, String> properties = new HashMap<>();
	}
}
