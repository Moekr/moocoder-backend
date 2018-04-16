package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ProblemService;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProblemController {
	private final ProblemService problemService;

	@Autowired
	public ProblemController(ProblemService problemService) {
		this.problemService = problemService;
	}

	@PostMapping("/problem")
	public Map<String, Object> create(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam MultipartFile file) throws ServiceException, IOException {
		byte[] content = file.getBytes();
		if (userDetails.isTeacher()) {
			return ToolKit.assemblyResponseBody(problemService.create(userDetails.getId(), content));
		} else if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(problemService.create(content));
		}
		throw new AccessDeniedException();
	}
}
