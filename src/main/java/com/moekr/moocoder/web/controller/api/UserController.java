package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.UserService;
import com.moekr.moocoder.logic.vo.UserVO;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.UserDTO;
import com.moekr.moocoder.web.dto.form.ChangePasswordForm;
import com.moekr.moocoder.web.response.EmptyResponse;
import com.moekr.moocoder.web.response.PageResourceResponse;
import com.moekr.moocoder.web.response.ResourceResponse;
import com.moekr.moocoder.web.response.Response;
import com.moekr.moocoder.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import static com.moekr.moocoder.web.security.WebSecurityConstants.*;

@RestController
@RequestMapping("/api")
public class UserController extends AbstractApiController {
	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/user")
	@RolesAllowed(ADMIN_ROLE)
	public Response create(@RequestBody @Validated(PostMapping.class) UserDTO userDTO, Errors errors) throws ServiceException {
		checkErrors(errors);
		return new ResourceResponse(userService.create(userDTO));
	}

	@GetMapping("/user")
	@RolesAllowed(ADMIN_ROLE)
	public Response retrievePage(@RequestParam(defaultValue = "1") int page,
								 @RequestParam(defaultValue = "10") int limit,
								 @RequestParam(defaultValue = "") String search) throws ServiceException {
		return new PageResourceResponse(userService.retrievePage(page, limit, search));
	}

	@GetMapping("/user/{userId:\\d+}")
	@RolesAllowed(ADMIN_ROLE)
	public Response retrieve(@PathVariable int userId) throws ServiceException {
		return new ResourceResponse(userService.retrieve(userId));
	}

	@GetMapping("/user/current")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(UserVO.ADMIN);
		}
		return new ResourceResponse(userService.retrieve(userDetails.getId()));
	}

	@DeleteMapping("/user/{userId:\\d+}")
	@RolesAllowed(ADMIN_ROLE)
	public Response delete(@PathVariable int userId) throws ServiceException {
		userService.delete(userId);
		return new EmptyResponse();
	}

	@PostMapping("/user/password/change")
	@RolesAllowed({STUDENT_ROLE, TEACHER_ROLE})
	public Response changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
								   @RequestBody @Valid ChangePasswordForm form) throws ServiceException {
		userService.changePassword(userDetails.getId(), form);
		return new EmptyResponse();
	}

	@PostMapping("/user/{userId:\\d+}/password/reset")
	@RolesAllowed(ADMIN_ROLE)
	public Response resetPassword(@PathVariable int userId) throws ServiceException {
		userService.resetPassword(userId);
		return new EmptyResponse();
	}
}
