package com.moekr.aes.logic.service.impl;

import com.moekr.aes.logic.service.MailService;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.AesProperties.Mail;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

@Service
@CommonsLog
public class MailServiceImpl implements MailService {
	private final TemplateEngine templateEngine;
	private final JavaMailSender mailSender;

	private final String from;
	private final String personal;

	@Autowired
	public MailServiceImpl(TemplateEngine templateEngine, JavaMailSender mailSender, AesProperties properties) {
		this.templateEngine = templateEngine;
		this.mailSender = mailSender;

		Mail mail = properties.getMail();
		this.from = mail.getFrom();
		this.personal = mail.getPersonal();
	}

	@Override
	@Async
	public void send(String to, String personal, String subject, ModelAndView modelAndView) {
		if (StringUtils.isBlank(from)) return;
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			try {
				helper.setFrom(from, this.personal);
			} catch (UnsupportedEncodingException e) {
				helper.setFrom(from);
			}
			try {
				helper.setTo(new InternetAddress(to, personal));
			} catch (UnsupportedEncodingException e) {
				helper.setTo(to);
			}
			helper.setSubject(subject);
			helper.setText(templateEngine.process(modelAndView.getViewName(), new Context(Locale.CHINA, modelAndView.getModel())), true);
		} catch (MessagingException e) {
			log.warn("生成发送给[" + to + "(" + personal + ")]的邮件[" + subject + "]时发生异常：" + e.getMessage());
			return;
		}
		try {
			mailSender.send(message);
		} catch (MailException e) {
			log.warn("投递发送给[" + to + "(" + personal + ")]的邮件[" + subject + "]时发生异常：" + e.getMessage());
		}
	}
}
