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
public class ExaminationController {
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

	@PostMapping("/examination/{examinationId}/participate")
	public Map<String, Object> participate(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable int examinationId, Errors errors) throws ServiceException {
		if (userDetails.isStudent()) {
			if (errors.hasErrors()) {
				throw new InvalidRequestException(errors.getGlobalError().getDefaultMessage());
			}
			examinationService.participate(userDetails.getId(), examinationId);
			return ToolKit.emptyResponseBody();
		}
		throw new AccessDeniedException();
	}
}
