package com.moekr.aes.web.controller.internal;

import com.moekr.aes.logic.service.NotifyService;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal/notify")
public class NotifyController {
	private final String secret;
	private final NotifyService notifyService;

	@Autowired
	public NotifyController(AesProperties properties, NotifyService notifyService) {
		this.secret = properties.getLocal().getSecret();
		this.notifyService = notifyService;
	}

	@PostMapping(value = "/webhook/{id:\\d+}", params = "secret")
	public Map<String, Object> webHook(@PathVariable int id, @RequestParam String secret) throws ServiceException {
		if (!this.secret.equals(secret)) {
			throw new AccessDeniedException();
		}
		notifyService.webHook(id);
		return ToolKit.emptyResponseBody();
	}

	@PostMapping(value = "/callback/{id:\\d+}/{buildNumber:\\d+}", params = "secret")
	public Map<String, Object> callback(@PathVariable int id, @PathVariable int buildNumber, @RequestParam String secret) throws ServiceException {
		if (!this.secret.equals(secret)) {
			throw new AccessDeniedException();
		}
		notifyService.callback(id, buildNumber);
		return ToolKit.emptyResponseBody();
	}
}
