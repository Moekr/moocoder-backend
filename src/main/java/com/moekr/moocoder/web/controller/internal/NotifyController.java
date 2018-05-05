package com.moekr.moocoder.web.controller.internal;

import com.moekr.moocoder.logic.service.NotifyService;
import com.moekr.moocoder.util.ApplicationProperties;
import com.moekr.moocoder.util.exceptions.AccessDeniedException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.webhook.WebHookDTO;
import com.moekr.moocoder.web.response.EmptyResponse;
import com.moekr.moocoder.web.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/internal/notify", headers = "X-Moocoder-Secret")
public class NotifyController {
	private final String secret;
	private final NotifyService notifyService;

	@Autowired
	public NotifyController(ApplicationProperties properties, NotifyService notifyService) {
		this.secret = properties.getSecret();
		this.notifyService = notifyService;
	}

	@PostMapping("/webhook/{id:\\d+}")
	public Response webHook(@PathVariable int id,
							@RequestHeader("X-Moocoder-Secret") String secret,
							@RequestBody WebHookDTO webHookDTO) throws ServiceException {
		if (!this.secret.equals(secret)) {
			throw new AccessDeniedException();
		}
		notifyService.webHook(id, webHookDTO.getCheckoutSha());
		return new EmptyResponse();
	}

	@PostMapping("/callback/{id:\\d+}/{buildNumber:\\d+}")
	public Response callback(@PathVariable int id,
							 @PathVariable int buildNumber,
							 @RequestHeader("X-Moocoder-Secret") String secret) throws ServiceException {
		if (!this.secret.equals(secret)) {
			throw new AccessDeniedException();
		}
		notifyService.callback(id, buildNumber);
		return new EmptyResponse();
	}
}
