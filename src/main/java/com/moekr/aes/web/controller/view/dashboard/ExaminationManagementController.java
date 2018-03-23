package com.moekr.aes.web.controller.view.dashboard;

import com.moekr.aes.logic.service.ExaminationService;
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
@RequestMapping("/dashboard/examination")
public class ExaminationManagementController {
	private final ExaminationService examinationService;

	@Autowired
	public ExaminationManagementController(ExaminationService examinationService) {
		this.examinationService = examinationService;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		model.addAttribute("examinationList", examinationService.findAll());
		return "dashboard/examination/index";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PostMapping({"/", "/index.html"})
	public String index(@RequestParam("id") Optional<Integer> examinationId, Model model) {
		if (!examinationId.isPresent()) return "redirect:/dashboard/examination/";
		try {
			examinationService.delete(examinationId.get());
			model.addAttribute("success", "操作成功！");
		} catch (ServiceException e) {
			model.addAttribute("error", e.getMessage());
		}
		model.addAttribute("examinationList", examinationService.findAll());
		return "dashboard/examination/index";
	}
}
