package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.StatisticService;
import com.moekr.moocoder.web.response.ResourceResponse;
import com.moekr.moocoder.web.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

import static com.moekr.moocoder.web.security.WebSecurityConstants.ADMIN_ROLE;

@RestController
@RequestMapping("/api/statistic")
public class StatisticController {
	private final StatisticService statisticService;

	@Autowired
	public StatisticController(StatisticService statisticService) {
		this.statisticService = statisticService;
	}

	@GetMapping
	@RolesAllowed(ADMIN_ROLE)
	public Response statistic() {
		return new ResourceResponse(statisticService.statistic());
	}
}
