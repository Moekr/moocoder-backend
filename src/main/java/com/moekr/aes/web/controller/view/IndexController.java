package com.moekr.aes.web.controller.view;

import com.moekr.aes.util.ToolKit;
import com.moekr.aes.web.security.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
	@GetMapping("/")
	public String index() {
		UserDetails userDetails = ToolKit.currentUserDetails();
		if (userDetails == null) return "redirect:/login.html";
		if (userDetails.getAuthorities().contains(WebSecurityConfiguration.STUDENT_AUTHORITY)) return "redirect:/s/";
		if (userDetails.getAuthorities().contains(WebSecurityConfiguration.TEACHER_AUTHORITY)) return "redirect:/t/";
		if (userDetails.getAuthorities().contains(WebSecurityConfiguration.ADMIN_AUTHORITY)) return "redirect:/dashboard/";
		return "redirect:/login.html";
	}
}
