package com.moekr.aes.util;

import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class ToolKit {
	public static final String BANNER = "AES";
	public static final String VERSION = "0.3.0-SNAPSHOT";

	public static Map<String, Object> emptyResponseBody() {
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("err", 0);
		return responseBody;
	}

	public static Map<String, Object> assemblyResponseBody(Object res) {
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("err", 0);
		responseBody.put("res", res);
		return responseBody;
	}

	public static Map<String, Object> assemblyResponseBody(Page res) {
		Map<String, Object> responseBody = assemblyResponseBody(res.getContent());
		responseBody.put("page", new PageInfo(res));
		return responseBody;
	}

	@Data
	private static class PageInfo {
		private int page;
		private int limit;
		private int size;
		private long total;

		PageInfo(Page page) {
			this.page = page.getNumber() + 1;
			this.limit = page.getSize();
			this.size = page.getContent().size();
			this.total = page.getTotalElements();
		}
	}

	public static Map<String, Object> assemblyResponseBody(int error, String message) {
		Map<String, Object> responseBody = new HashMap<>();
		responseBody.put("err", error);
		responseBody.put("msg", message);
		return responseBody;
	}

	public static HttpStatus httpStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode == null) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		try {
			return HttpStatus.valueOf(statusCode);
		} catch (Exception e) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

	public static UserDetails currentUserDetails() {
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		Object principle = authentication.getPrincipal();
		return (UserDetails) principle;
	}

	public static boolean hasLogin() {
		try {
			return currentUserDetails() != null;
		} catch (Exception e) {
			return false;
		}
	}

	public static String randomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static String randomPassword() {
		return RandomStringUtils.randomAlphanumeric(12);
	}

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

	public static LocalDateTime parse(String str) {
		return LocalDateTime.parse(str, FORMATTER);
	}

	public static Date convert(long timestamp) {
		return new Date(timestamp * DateUtils.MILLIS_PER_SECOND);
	}
}
