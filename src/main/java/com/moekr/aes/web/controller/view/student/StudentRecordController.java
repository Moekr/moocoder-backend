package com.moekr.aes.web.controller.view.student;

import com.moekr.aes.logic.service.RecordService;
import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.logic.vo.model.UserModel;
import com.moekr.aes.util.ToolKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/s/record")
public class StudentRecordController {
	private final UserService userService;
	private final RecordService recordService;

	@Autowired
	public StudentRecordController(UserService userService, RecordService recordService) {
		this.userService = userService;
		this.recordService = recordService;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		model.addAttribute("recordList", recordService.findAll(user.getId()));
		return "student/record/index";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@GetMapping("/detail.html")
	public String detail(@RequestParam("r") Optional<Integer> recordId, Model model) {
		if (!recordId.isPresent()) return "redirect:/s/record/";
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		model.addAttribute("record", recordService.findById(user.getId(), recordId.get()));
		return "student/record/detail";
	}
}
