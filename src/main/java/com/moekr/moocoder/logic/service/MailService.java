package com.moekr.moocoder.logic.service;

import org.springframework.web.servlet.ModelAndView;

public interface MailService {
	void send(String to, String personal, String subject, ModelAndView modelAndView);
}
