package com.moekr.aes.util;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
@Configuration
@ConfigurationProperties("aes")
public class AesProperties {
	private Local local = new Local();
	private Gitlab gitlab = new Gitlab();
	private Jenkins jenkins = new Jenkins();
	private Mail mail = new Mail();
	private Storage storage = new Storage();

	@Data
	@EqualsAndHashCode
	@ToString
	public static class Local {
		private String host;
		private String secret;
	}

	@Data
	@EqualsAndHashCode
	@ToString
	public static class Gitlab {
		private String host;
		private String proxy;
		private String username;
		private String token;

		public String getProxy() {
			return StringUtils.defaultString(proxy, host);
		}
	}

	@Data
	@EqualsAndHashCode
	@ToString
	public static class Jenkins {
		private String host;
		private String username;
		private String token;
		private String credential;
		private Boolean deleteAfterClose = true;
	}

	@Data
	@EqualsAndHashCode
	@ToString
	public static class Mail {
		private String from;
		private String personal = "Automated Examination System";
	}

	@Data
	@EqualsAndHashCode
	@ToString
	public static class Storage {
		private String host = "";
		private String type = "local";
		private Map<String, String> properties = new HashMap<>();
	}
}
