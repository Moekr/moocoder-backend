package com.moekr.moocoder.web.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController extends AbstractViewController {
	@GetMapping("/")
	public String index() {
		return "index";
	}
}
