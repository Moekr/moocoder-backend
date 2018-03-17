package com.moekr.aes.web.controller.view.dashboard;

import com.moekr.aes.logic.service.ExaminationService;
import com.moekr.aes.logic.service.StatisticService;
import com.moekr.aes.util.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class DashboardIndexController {
	private final ExaminationService examinationService;
	private final StatisticService statisticService;

	@Autowired
	public DashboardIndexController(ExaminationService examinationService, StatisticService statisticService) {
		this.examinationService = examinationService;
		this.statisticService = statisticService;
	}

	@GetMapping({"/", "/index.html"})
	public String dashboard(Model model) {
		model.addAttribute("statistic", statisticService.statistic());
		model.addAttribute("examinationList", examinationService.findAll().stream().filter(e -> e.getStatus() == Status.ONGOING).collect(Collectors.toList()));
		return "dashboard/index";
	}
}
