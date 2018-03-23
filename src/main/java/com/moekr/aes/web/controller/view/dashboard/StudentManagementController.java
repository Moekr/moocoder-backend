package com.moekr.aes.web.controller.view.dashboard;

import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.enums.Role;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/dashboard/student/")
public class StudentManagementController {
	private final UserService userService;

	public StudentManagementController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		model.addAttribute("userList", userService.findAllByRole(Role.STUDENT));
		return "dashboard/student/index";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PostMapping({"/", "/index.html"})
	public String index(@RequestParam("id") Optional<Integer> userId, Model model) {
		if (!userId.isPresent()) return "redirect:/dashboard/student/";
		try {
			userService.delete(userId.get());
			model.addAttribute("success", "操作成功！");
		} catch (ServiceException e) {
			model.addAttribute("error", e.getMessage());
		}
		model.addAttribute("userList", userService.findAllByRole(Role.STUDENT));
		return "dashboard/student/index";
	}
}
