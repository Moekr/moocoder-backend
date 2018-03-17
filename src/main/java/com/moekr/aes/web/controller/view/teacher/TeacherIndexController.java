package com.moekr.aes.web.controller.view.teacher;

import com.moekr.aes.logic.service.ExaminationService;
import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.logic.vo.model.ExaminationModel;
import com.moekr.aes.logic.vo.model.UserModel;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/t")
public class TeacherIndexController {
	private final UserService userService;
	private final ExaminationService examinationService;

	@Autowired
	public TeacherIndexController(UserService userService, ExaminationService examinationService) {
		this.userService = userService;
		this.examinationService = examinationService;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		List<ExaminationModel> examinationList = examinationService.findAll(user.getId());
		examinationList.removeIf(e -> e.getStatus() != Status.ONGOING);
		model.addAttribute("user", user);
		model.addAttribute("examinationList", examinationList);
		return "teacher/index";
	}

	@GetMapping("/help.html")
	public String help(Model model) {
		model.addAttribute("user", userService.findByUsername(ToolKit.currentUserDetails().getUsername()));
		return "teacher/help";
	}
}
