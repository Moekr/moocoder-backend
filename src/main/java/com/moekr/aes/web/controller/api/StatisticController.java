package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.StatisticService;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.response.ResourceResponse;
import com.moekr.aes.web.response.Response;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistic")
public class StatisticController {
	private final StatisticService statisticService;

	@Autowired
	public StatisticController(StatisticService statisticService) {
		this.statisticService = statisticService;
	}

	@GetMapping
	public Response statistic(@AuthenticationPrincipal CustomUserDetails userDetails) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(statisticService.statistic());
		}
		throw new AccessDeniedException();
	}
}
