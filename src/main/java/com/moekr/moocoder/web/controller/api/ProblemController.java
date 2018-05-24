package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.ProblemService;
import com.moekr.moocoder.util.enums.ProblemType;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.ProblemDTO;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

import static com.moekr.moocoder.web.security.WebSecurityConstants.ADMIN_ROLE;
import static com.moekr.moocoder.web.security.WebSecurityConstants.TEACHER_ROLE;

@RestController
@RequestMapping("/api")
@RolesAllowed({TEACHER_ROLE, ADMIN_ROLE})
public class ProblemController extends AbstractApiController {
	private final ProblemService problemService;

	@Autowired
	public ProblemController(ProblemService problemService) {
		this.problemService = problemService;
	}

	@PostMapping("/problem")
	public Response create(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @RequestPart("meta") @Validated(PostMapping.class) ProblemDTO problemDTO, Errors errors,
						   @RequestPart("data") MultipartFile file) throws ServiceException, IOException {
		checkErrors(errors);
		byte[] content = file.getBytes();
		return new ResourceResponse(problemService.create(userDetails.getId(), problemDTO, content));
	}

	@GetMapping("/problem")
	public Response retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
								 @RequestParam(defaultValue = "1") int page,
								 @RequestParam(defaultValue = "10") int limit,
								 @RequestParam(name = "type", defaultValue = "") String typeStr) throws ServiceException {
		ProblemType type;
		try {
			type = ProblemType.valueOf(typeStr);
		} catch (IllegalArgumentException e) {
			type = null;
		}
		if (userDetails.isAdmin()) {
			return new PageResourceResponse(problemService.retrievePage(page, limit, type));
		}
		return new PageResourceResponse(problemService.retrievePage(userDetails.getId(), page, limit, type));
	}

	@GetMapping("/problem/{problemId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int problemId) throws ServiceException {
		return new ResourceResponse(problemService.retrieve(userDetails.getId(), problemId));
	}

	@PutMapping("/problem/{problemId:\\d+}")
	public Response update(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int problemId,
						   @RequestPart String path,
						   @RequestPart MultipartFile file) throws ServiceException, IOException {
		byte[] content = file.getBytes();
		problemService.update(userDetails.getId(), problemId, path, content);
		return new EmptyResponse();
	}

	@DeleteMapping("/problem/{problemId:\\d+}")
	public Response delete(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int problemId) throws ServiceException {
		problemService.delete(userDetails.getId(), problemId);
		return new EmptyResponse();
	}
}
