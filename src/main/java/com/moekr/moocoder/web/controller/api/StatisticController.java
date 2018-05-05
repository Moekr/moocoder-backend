package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.StatisticService;
import com.moekr.moocoder.util.exceptions.AccessDeniedException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.response.ResourceResponse;
import com.moekr.moocoder.web.response.Response;
import com.moekr.moocoder.web.security.impl.CustomUserDetails;
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
