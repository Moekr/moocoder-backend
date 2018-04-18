package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.NotifyService;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.InvalidRequestException;
import com.moekr.aes.util.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notify")
public class CallbackController {
	private final String secret;
	private final NotifyService notifyService;

	@Autowired
	public CallbackController(AesProperties properties, NotifyService notifyService) {
		this.secret = properties.getSecret();
		this.notifyService = notifyService;
	}

	@PostMapping("/callback/{id}/{buildNumber}")
	public Map<String, Object> callback(@PathVariable int id, @PathVariable int buildNumber, @RequestParam String secret, Errors errors) throws ServiceException {
		if (errors.hasErrors()) {
			throw new InvalidRequestException(errors.getGlobalError().getDefaultMessage());
		}
		if (!this.secret.equals(secret)) {
			throw new AccessDeniedException();
		}
		notifyService.callback(id, buildNumber);
		return ToolKit.emptyResponseBody();
	}
}
