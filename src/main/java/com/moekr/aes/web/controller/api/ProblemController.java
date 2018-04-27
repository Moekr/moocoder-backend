package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ProblemService;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ProblemDTO;
import com.moekr.aes.web.response.EmptyResponse;
import com.moekr.aes.web.response.PageResourceResponse;
import com.moekr.aes.web.response.ResourceResponse;
import com.moekr.aes.web.response.Response;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
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
		if (userDetails.isTeacher()) {
			return new ResourceResponse(problemService.create(userDetails.getId(), problemDTO, content));
		} else if (userDetails.isAdmin()) {
			return new ResourceResponse(problemService.create(problemDTO, content));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/problem")
	public Response retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
								 @RequestParam(defaultValue = "1") int page,
								 @RequestParam(defaultValue = "10") int limit) throws ServiceException {
		if (userDetails.isTeacher()) {
			return new PageResourceResponse(problemService.retrievePage(userDetails.getId(), page, limit));
		} else if (userDetails.isAdmin()) {
			return new PageResourceResponse(problemService.retrievePage(page, limit));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/problem/{problemId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int problemId) throws ServiceException {
		if (userDetails.isTeacher()) {
			return new ResourceResponse(problemService.retrieve(userDetails.getId(), problemId));
		} else if (userDetails.isAdmin()) {
			return new ResourceResponse(problemService.retrieve(problemId));
		}
		throw new AccessDeniedException();
	}

	@PutMapping("/problem/{problemId:\\d+}")
	public Response update(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int problemId,
						   @RequestBody @Validated(PutMapping.class) ProblemDTO problemDTO, Errors errors) throws ServiceException {
		checkErrors(errors);
		if (userDetails.isTeacher()) {
			return new ResourceResponse(problemService.update(userDetails.getId(), problemId, problemDTO));
		} else if (userDetails.isAdmin()) {
			return new ResourceResponse(problemService.update(problemId, problemDTO));
		}
		throw new AccessDeniedException();
	}

	@DeleteMapping("/problem/{problemId:\\d+}")
	public Response delete(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int problemId) throws ServiceException {
		if (userDetails.isTeacher()) {
			problemService.delete(userDetails.getId(), problemId);
		} else if (userDetails.isAdmin()) {
			problemService.delete(problemId);
		} else {
			throw new AccessDeniedException();
		}
		return new EmptyResponse();
	}

	@PostMapping("/problem/{problemId:\\d+}/deprecate")
	public Response deprecate(@AuthenticationPrincipal CustomUserDetails userDetails,
							  @PathVariable int problemId) throws ServiceException {
		if (userDetails.isTeacher()) {
			problemService.deprecate(userDetails.getId(), problemId);
		} else if (userDetails.isAdmin()) {
			problemService.deprecate(problemId);
		}
		return new EmptyResponse();
	}
}
