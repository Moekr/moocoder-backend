package com.moekr.aes.web.controller.api;

import com.moekr.aes.util.ToolKit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthorityController {
	@PostMapping("/authority")
	public Map<String, Object> authority() {
		return ToolKit.emptyResponseBody();
	}
}
