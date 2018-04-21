package com.moekr.aes.web.controller.internal;

import com.moekr.aes.logic.service.NotifyService;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notify")
public class NotifyController {
	private final String secret;
	private final NotifyService notifyService;

	@Autowired
	public NotifyController(AesProperties properties, NotifyService notifyService) {
		this.secret = properties.getSecret();
		this.notifyService = notifyService;
	}

	@PostMapping(value = "/webhook/{id:\\d+}", params = "secret")
	public Response webHook(@PathVariable int id, @RequestParam String secret) throws ServiceException {
		if (!this.secret.equals(secret)) {
			throw new AccessDeniedException();
		}
		notifyService.webHook(id);
		return new Response();
	}

	@PostMapping(value = "/callback/{id:\\d+}/{buildNumber:\\d+}", params = "secret")
	public Response callback(@PathVariable int id, @PathVariable int buildNumber, @RequestParam String secret) throws ServiceException {
		if (!this.secret.equals(secret)) {
			throw new AccessDeniedException();
		}
		notifyService.callback(id, buildNumber);
		return new Response();
	}
}
