package com.moekr.moocoder.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

@Configuration
public class FlashMapManagerConfiguration {
	@Bean
	public FlashMapManager flashMapManager() {
		return new SessionFlashMapManager();
	}
}
