package com.moekr.aes.web.controller.view.student;

import com.moekr.aes.logic.service.ExaminationService;
import com.moekr.aes.logic.service.ResultService;
import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.logic.vo.model.UserModel;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.ToolKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/s/examination")
public class StudentExaminationController {
	private final UserService userService;
	private final ExaminationService examinationService;
	private final ResultService resultService;
	private final AesProperties properties;

	@Autowired
	public StudentExaminationController(UserService userService, ExaminationService examinationService, ResultService resultService, AesProperties properties) {
		this.userService = userService;
		this.examinationService = examinationService;
		this.resultService = resultService;
		this.properties = properties;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		model.addAttribute("examinationList", examinationService.findAll(user.getId()));
		model.addAttribute("allExaminationList", examinationService.findAll());
		return "student/examination/index";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PostMapping({"/", "/index.html"})
	public String index(@RequestParam("id") Optional<Integer> examinationId, Model model) {
		if (!examinationId.isPresent()) return "redirect:/s/examination/";
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		try {
			examinationService.participate(user.getId(), examinationId.get());
			model.addAttribute("success", "操作成功！");
		} catch (ServiceException e) {
			model.addAttribute("error", e.getMessage());
		}
		model.addAttribute("user", user);
		model.addAttribute("examinationList", examinationService.findAll(user.getId()));
		model.addAttribute("allExaminationList", examinationService.findAll());
		return "student/examination/index";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@GetMapping("/detail.html")
	public String detail(@RequestParam("e") Optional<Integer> examinationId, Model model) {
		if (!examinationId.isPresent()) return "redirect:/s/examination/";
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		model.addAttribute("host", properties.getGitlab().getHost());
		model.addAttribute("examination", examinationService.findById(user.getId(), examinationId.get()));
		model.addAttribute("result", resultService.findByExamination(user.getId(), examinationId.get()));
		return "student/examination/detail";
	}
}
