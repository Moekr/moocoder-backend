package com.moekr.aes.web.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class LoginController extends AbstractViewController {
	@GetMapping("/login.html")
	public String login(@ModelAttribute("from") String from, Model model) {
		if (isLogin()) {
			return "redirect:/";
		} else if ("register".equals(from)) {
			model.addAttribute("success", "注册成功！");
		} else if ("logout".equals(from)) {
			model.addAttribute("success", "您已成功退出登录！");
		} else if ("login".equals(from)) {
			model.addAttribute("error", "用户不存在或密码错误！");
		}
		return "login";
	}
}
