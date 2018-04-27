package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ExamService;
import com.moekr.aes.util.enums.ExamStatus;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ExamDTO;
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

@RestController
@RequestMapping("/api")
public class ExamController extends AbstractApiController {
	private final ExamService examService;

	@Autowired
	public ExamController(ExamService examService) {
		this.examService = examService;
	}

	@PostMapping("/exam")
	public Response create(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @RequestBody @Validated(PostMapping.class) ExamDTO examDTO, Errors errors) throws ServiceException {
		if (userDetails.isTeacher()) {
			checkErrors(errors);
			return new ResourceResponse(examService.create(userDetails.getId(), examDTO));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/exam")
	public Response retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
								 @RequestParam(defaultValue = "1") int page,
								 @RequestParam(defaultValue = "10") int limit,
								 @RequestParam(name = "status", defaultValue = "") String statusStr) throws ServiceException {
		boolean joined = statusStr.equals("JOINED");
		ExamStatus status;
		try {
			status = ExamStatus.valueOf(statusStr);
		} catch (IllegalArgumentException e) {
			status = null;
		}
		if (userDetails.isTeacher()) {
			return new PageResourceResponse(examService.retrievePage(userDetails.getId(), page, limit, status));
		} else if (userDetails.isAdmin()) {
			return new PageResourceResponse(examService.retrievePage(page, limit, status));
		} else {
			return new PageResourceResponse(examService.retrievePage(userDetails.getId(), page, limit, joined, status));
		}
	}

	@GetMapping("/exam/{examId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int examId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(examService.retrieve(examId));
		}
		return new ResourceResponse(examService.retrieve(userDetails.getId(), examId));
	}

	@PutMapping("/exam/{examId:\\d+}")
	public Response update(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int examId,
						   @RequestBody @Validated(PutMapping.class) ExamDTO examDTO, Errors errors) throws ServiceException {
		checkErrors(errors);
		if (userDetails.isTeacher()) {
			return new ResourceResponse(examService.update(userDetails.getId(), examId, examDTO));
		} else if (userDetails.isAdmin()) {
			return new ResourceResponse(examService.update(examId, examDTO));
		}
		throw new AccessDeniedException();
	}

	@DeleteMapping("/exam/{examId:\\d+}")
	public Response delete(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int examId) throws ServiceException {
		if (userDetails.isTeacher()) {
			examService.delete(userDetails.getId(), examId);
		} else if (userDetails.isAdmin()) {
			examService.delete(examId);
		} else {
			throw new AccessDeniedException();
		}
		return new EmptyResponse();
	}

	@PostMapping("/exam/{examId:\\d+}/join")
	public Response join(@AuthenticationPrincipal CustomUserDetails userDetails,
						 @PathVariable int examId) throws ServiceException {
		if (userDetails.isStudent()) {
			examService.join(userDetails.getId(), examId);
			return new EmptyResponse();
		}
		throw new AccessDeniedException();
	}
}
