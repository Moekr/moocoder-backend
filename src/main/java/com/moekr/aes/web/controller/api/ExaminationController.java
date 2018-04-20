package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ExaminationService;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.InvalidRequestException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.dto.ExaminationDTO;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExaminationController extends AbstractApiController {
	private final ExaminationService examinationService;

	@Autowired
	public ExaminationController(ExaminationService examinationService) {
		this.examinationService = examinationService;
	}

	@PostMapping("/examination")
	public Map<String, Object> create(@AuthenticationPrincipal CustomUserDetails userDetails,
									  @RequestBody @Validated(PostMapping.class) ExaminationDTO examinationDTO, Errors errors) throws ServiceException {
		if (userDetails.isTeacher()) {
			if (errors.hasErrors()) {
				throw new InvalidRequestException(errors.getGlobalError().getDefaultMessage());
			}
			return ToolKit.assemblyResponseBody(examinationService.create(userDetails.getId(), examinationDTO));
		}
		throw new AccessDeniedException();
	}

	@GetMapping("/examination")
	public Map<String, Object> retrievePage(@AuthenticationPrincipal CustomUserDetails userDetails,
											@RequestParam(defaultValue = "1") int page,
											@RequestParam(defaultValue = "10") int limit) throws ServiceException {
		if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(examinationService.retrievePage(page, limit));
		} else {
			return ToolKit.assemblyResponseBody(examinationService.retrievePage(userDetails.getId(), page, limit));
		}
	}

	@PostMapping("/examination/{examinationId:\\d+}/participate")
	public Map<String, Object> participate(@AuthenticationPrincipal CustomUserDetails userDetails,
										   @PathVariable int examinationId) throws ServiceException {
		if (userDetails.isStudent()) {
			examinationService.participate(userDetails.getId(), examinationId);
			return ToolKit.emptyResponseBody();
		}
		throw new AccessDeniedException();
	}
}
