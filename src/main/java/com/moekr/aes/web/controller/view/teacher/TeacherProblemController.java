package com.moekr.aes.web.controller.view.teacher;

import com.moekr.aes.logic.service.ProblemService;
import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.logic.vo.model.UserModel;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.ToolKit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/t/problem")
public class TeacherProblemController {
	private final UserService userService;
	private final ProblemService problemService;
	private final AesProperties properties;

	public TeacherProblemController(UserService userService, ProblemService problemService, AesProperties properties) {
		this.userService = userService;
		this.problemService = problemService;
		this.properties = properties;
	}

	@GetMapping({"/", "/index.html"})
	public String problem(Model model) {
		model.addAttribute("user", userService.findByUsername(ToolKit.currentUserDetails().getUsername()));
		model.addAttribute("host", properties.getStorage().getHost());
		model.addAttribute("problemList", problemService.findAll());
		return "teacher/problem/index";
	}

	@PostMapping({"/", "/index.html"})
	public String problem(@RequestParam MultipartFile file, Model model) throws IOException {
		UserModel user = userService.findByUsername(ToolKit.currentUserDetails().getUsername());
		model.addAttribute("user", user);
		try {
			problemService.upload(user.getId(), file.getBytes());
			model.addAttribute("success", "上传成功！");
		} catch (ServiceException e) {
			model.addAttribute("error", e.getMessage());
		}
		model.addAttribute("host", properties.getStorage().getHost());
		model.addAttribute("problemList", problemService.findAll());
		return "teacher/problem/index";
	}
}
