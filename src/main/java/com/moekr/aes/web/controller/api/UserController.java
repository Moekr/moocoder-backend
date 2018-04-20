package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.UserDTO;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController extends AbstractApiController {
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
		checkErrors(errors);
		return ToolKit.assemblyResponseBody(userService.create(userDTO));
	}

	@GetMapping("/user")
	public Map<String, Object> retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
											@RequestParam(defaultValue = "1") int page,
											@RequestParam(defaultValue = "10") int limit) throws ServiceException {
		if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(userService.retrievePage(page, limit));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/user/{userId:\\d+}")
	public Map<String, Object> retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
										@PathVariable int userId) throws ServiceException {
		if (userDetails.isAdmin() || userDetails.getId() == userId) {
			return ToolKit.assemblyResponseBody(userService.retrieve(userId));
		}
		throw new AccessDeniedException();
	}

	@DeleteMapping("/user/{userId:\\d+}")
	public Map<String, Object> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
									  @PathVariable int userId) throws ServiceException {
		if (userDetails.isAdmin()) {
			userService.delete(userId);
			return ToolKit.emptyResponseBody();
		} else {
			throw new AccessDeniedException();
		}
	}
}
