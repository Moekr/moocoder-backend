package com.moekr.aes.web.controller.view.dashboard;

import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.enums.Role;
import com.moekr.aes.web.dto.form.TeacherRegisterForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/dashboard/teacher/")
public class TeacherManagementController {
	private final UserService userService;

	public TeacherManagementController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping({"/", "/index.html"})
	public String index(Model model) {
		model.addAttribute("userList", userService.findAllByRole(Role.TEACHER));
		return "dashboard/teacher/index";
	}

	@GetMapping("/register.html")
	public String register() {
		return "dashboard/teacher/register";
	}

	@PostMapping("/register.html")
	public String register(@ModelAttribute @Valid TeacherRegisterForm form, Errors errors, Model model) {
		if (errors.hasFieldErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
		} else {
			try {
				userService.register(form);
				model.addAttribute("success", "注册成功，页面将在5秒后跳转！");
			} catch (ServiceException e) {
				model.addAttribute("error", e.getMessage());
			}
		}
		return "dashboard/teacher/register";
	}
}
