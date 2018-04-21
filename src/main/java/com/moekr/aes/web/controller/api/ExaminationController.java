package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ExaminationService;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ExaminationDTO;
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
@RequestMapping("/api/examination")
public class ExaminationController extends AbstractApiController {
	private final ExaminationService examinationService;

	@Autowired
	public ExaminationController(ExaminationService examinationService) {
		this.examinationService = examinationService;
	}

	@PostMapping
	public Response create(@AuthenticationPrincipal CustomUserDetails userDetails,
						   @RequestBody @Validated(PostMapping.class) ExaminationDTO examinationDTO, Errors errors) throws ServiceException {
		if (userDetails.isTeacher()) {
			checkErrors(errors);
			return new ResourceResponse(examinationService.create(userDetails.getId(), examinationDTO));
		}
		throw new AccessDeniedException();
	}

	@GetMapping
	public Response retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
											@RequestParam(defaultValue = "1") int page,
											@RequestParam(defaultValue = "10") int limit) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new PageResourceResponse(examinationService.retrievePage(page, limit));
		} else {
			return new PageResourceResponse(examinationService.retrievePage(userDetails.getId(), page, limit));
		}
	}

	@PostMapping("/{examinationId:\\d+}/participate")
	public Response participate(@AuthenticationPrincipal CustomUserDetails userDetails,
										   @PathVariable int examinationId) throws ServiceException {
		if (userDetails.isStudent()) {
			examinationService.participate(userDetails.getId(), examinationId);
			return new Response();
		}
		throw new AccessDeniedException();
	}
}
