package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ProblemService;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ProblemDTO;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProblemController extends AbstractApiController {
	private final ProblemService problemService;

	@Autowired
	public ProblemController(ProblemService problemService) {
		this.problemService = problemService;
	}

	@PostMapping("/problem")
	public Map<String, Object> create(@AuthenticationPrincipal CustomUserDetails userDetails,
									  @RequestParam MultipartFile file) throws ServiceException, IOException {
		byte[] content = file.getBytes();
		if (userDetails.isTeacher()) {
			return ToolKit.assemblyResponseBody(problemService.create(userDetails.getId(), content));
		} else if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(problemService.create(content));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/problem")
	public Map<String, Object> retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
											@RequestParam(defaultValue = "1") int page,
											@RequestParam(defaultValue = "10") int limit) throws ServiceException {
		if (userDetails.isTeacher()) {
			return ToolKit.assemblyResponseBody(problemService.retrievePage(userDetails.getId(), page));
		} else if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(problemService.retrievePage(page, limit));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/problem/{problemId:\\d+}")
	public Map<String, Object> retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
										@PathVariable int problemId) throws ServiceException {
		if (userDetails.isTeacher()) {
			return ToolKit.assemblyResponseBody(problemService.retrieve(userDetails.getId(), problemId));
		} else if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(problemService.retrieve(problemId));
		}
		throw new AccessDeniedException();
	}

	@PutMapping("/problem/{problemId:\\d+}")
	public Map<String, Object> update(@AuthenticationPrincipal CustomUserDetails userDetails,
									  @PathVariable int problemId,
									  @RequestBody @Validated(PutMapping.class) ProblemDTO problemDTO, Errors errors) throws ServiceException {
		if (userDetails.isTeacher()) {
			checkErrors(errors);
			return ToolKit.assemblyResponseBody(problemService.update(userDetails.getId(), problemId, problemDTO));
		} else if (userDetails.isAdmin()) {
			checkErrors(errors);
			return ToolKit.assemblyResponseBody(problemService.update(problemId, problemDTO));
		}
		throw new AccessDeniedException();
	}

	@DeleteMapping("/problem/{problemId:\\d+}")
	public Map<String, Object> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
									  @PathVariable int problemId) throws ServiceException {
		if (userDetails.isTeacher()) {
			problemService.delete(userDetails.getId(), problemId);
		} else if (userDetails.isAdmin()) {
			problemService.delete(problemId);
		} else {
			throw new AccessDeniedException();
		}
		return ToolKit.emptyResponseBody();
	}
}
