package com.moekr.aes.web;

import com.moekr.aes.util.AesProperties;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@CommonsLog
public class WebMvcConfiguration implements WebMvcConfigurer {
	private final AesProperties properties;

	public WebMvcConfiguration(AesProperties properties) {
		this.properties = properties;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**").allowedMethods("POST", "GET", "PUT", "DELETE");
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		if (StringUtils.equals(properties.getStorage().getType(), "local")) {
			String location = properties.getStorage().getProperties().get("location") + File.separator;
			log.info("检测到使用本地存储，注册[/upload/**]到[" + location + "]");
			registry.addResourceHandler("/upload/**").addResourceLocations("file:" + location);
		}
	}

	@Override
	public void configurePathMatch(PathMatchConfigurer configurer) {
		configurer.setUseSuffixPatternMatch(false).setUseTrailingSlashMatch(false);
	}
}
