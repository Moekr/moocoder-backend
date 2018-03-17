package com.moekr.aes.web.controller.view;

import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.util.ServiceException;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.web.dto.form.ChangePasswordForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class PasswordController {
	private final UserService userService;

	@Autowired
	public PasswordController(UserService userService) {
		this.userService = userService;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@GetMapping("/password.html")
	public String password() {
		return "password";
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@PostMapping("/password.html")
	public String password(@ModelAttribute @Valid ChangePasswordForm form, Errors errors, Model model) {
		if (errors.hasFieldErrors()) {
			model.addAttribute("error", errors.getFieldError().getDefaultMessage());
		} else if (!StringUtils.equals(form.getPassword(), form.getConfirm())) {
			model.addAttribute("error", "两次输入的密码不一致！");
		} else {
			try {
				userService.changePassword(ToolKit.currentUserDetails().getUsername(), form);
				model.addAttribute("success", "密码修改成功，页面将在5秒后跳转！");
			} catch (ServiceException e) {
				model.addAttribute("error", e.getMessage());
			}
		}
		return "password";
	}
}
