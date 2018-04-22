package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.UserService;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.UserDTO;
import com.moekr.aes.web.response.PageResourceResponse;
import com.moekr.aes.web.response.ResourceResponse;
import com.moekr.aes.web.response.Response;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController extends AbstractApiController {
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public Response create(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @RequestBody @Validated(PostMapping.class) UserDTO userDTO, Errors errors) throws ServiceException {
		if (!userDetails.isAdmin()) {
			throw new AccessDeniedException();
		}
		checkErrors(errors);
		return new ResourceResponse(userService.create(userDTO));
	}

	@GetMapping
	public Response retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
								 @RequestParam(defaultValue = "1") int page,
								 @RequestParam(defaultValue = "10") int limit) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new PageResourceResponse(userService.retrievePage(page, limit));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/{userId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int userId) throws ServiceException {
		if (userDetails.isAdmin() || userDetails.getId() == userId) {
			return new ResourceResponse(userService.retrieve(userId));
		}
		throw new AccessDeniedException();
	}

	@DeleteMapping("/{userId:\\d+}")
	public Response delete(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int userId) throws ServiceException {
		if (userDetails.isAdmin()) {
			userService.delete(userId);
			return new Response();
		} else {
			throw new AccessDeniedException();
		}
	}
}
