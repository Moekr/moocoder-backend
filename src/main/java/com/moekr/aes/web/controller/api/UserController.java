package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.InvalidRequestException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.UserDTO;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/user")
	public Map<String, Object> create(@AuthenticationPrincipal CustomUserDetails userDetails,
									  @RequestBody @Validated(PostMapping.class) UserDTO userDTO, Errors errors) throws ServiceException {
		if (!userDetails.isAdmin()) {
			throw new AccessDeniedException();
		}
		if (errors.hasErrors()) {
			throw new InvalidRequestException(errors.getGlobalError().getDefaultMessage());
		}
		return ToolKit.assemblyResponseBody(userService.create(userDTO));
	}
}
