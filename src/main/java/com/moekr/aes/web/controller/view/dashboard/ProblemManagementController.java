package com.moekr.aes.web.controller.view.dashboard;

import com.moekr.aes.logic.service.ProblemService;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/dashboard/problem/")
public class ProblemManagementController {
	private final ProblemService problemService;
	private final AesProperties properties;

	@Autowired
	public ProblemManagementController(ProblemService problemService, AesProperties properties) {
		this.problemService = problemService;
		this.properties = properties;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		model.addAttribute("host", properties.getStorage().getHost());
		model.addAttribute("problemList", problemService.findAll());
		return "dashboard/problem/index";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PostMapping({"/", "/index.html"})
	public String index(@RequestParam("id") Optional<Integer> problemId, Model model) {
		if (!problemId.isPresent()) return "redirect:/dashboard/problem/";
		try {
			problemService.deprecate(problemId.get());
			model.addAttribute("success", "操作成功！");
		} catch (ServiceException e) {
			model.addAttribute("error", e.getMessage());
		}
		model.addAttribute("host", properties.getStorage().getHost());
		model.addAttribute("problemList", problemService.findAll());
		return "dashboard/problem/index";
	}
}
