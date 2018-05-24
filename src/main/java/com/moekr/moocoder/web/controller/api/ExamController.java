package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.ExamService;
import com.moekr.moocoder.util.enums.ExamStatus;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.dto.ExamDTO;
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

import static com.moekr.moocoder.web.security.WebSecurityConstants.*;

@RestController
@RequestMapping("/api")
public class ExamController extends AbstractApiController {
	private final ExamService examService;

	@Autowired
	public ExamController(ExamService examService) {
		this.examService = examService;
	}

	@PostMapping("/exam")
	@RolesAllowed(TEACHER_ROLE)
	public Response create(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @RequestBody @Validated(PostMapping.class) ExamDTO examDTO, Errors errors) throws ServiceException {
		checkErrors(errors);
		return new ResourceResponse(examService.create(userDetails.getId(), examDTO));
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
		}
		return new PageResourceResponse(examService.retrievePage(userDetails.getId(), page, limit, joined, status));
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
	@RolesAllowed({TEACHER_ROLE, ADMIN_ROLE})
	public Response update(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int examId,
						   @RequestBody @Validated(PutMapping.class) ExamDTO examDTO, Errors errors) throws ServiceException {
		checkErrors(errors);
		return new ResourceResponse(examService.update(userDetails.getId(), examId, examDTO));
	}

	@DeleteMapping("/exam/{examId:\\d+}")
	@RolesAllowed({TEACHER_ROLE, ADMIN_ROLE})
	public Response delete(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @PathVariable int examId) throws ServiceException {
		examService.delete(userDetails.getId(), examId);
		return new EmptyResponse();
	}

	@PostMapping("/exam/{examId:\\d+}/join")
	@RolesAllowed({STUDENT_ROLE, TEACHER_ROLE})
	public Response join(@AuthenticationPrincipal CustomUserDetails userDetails,
						 @PathVariable int examId) throws ServiceException {
		examService.join(userDetails.getId(), examId);
		return new EmptyResponse();
	}
}
