package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ResultService;
import com.moekr.aes.util.exceptions.AccessDeniedException;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.response.ResourceResponse;
import com.moekr.aes.web.response.Response;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ResultController extends AbstractApiController {
	private final ResultService resultService;

	@Autowired
	public ResultController(ResultService resultService) {
		this.resultService = resultService;
	}

	@GetMapping("/result/{resultId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int resultId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(resultService.retrieve(resultId));
		} else {
			return new ResourceResponse(resultService.retrieve(userDetails.getId(), resultId));
		}
	}

	@GetMapping("/examination/{examinationId:\\d+}/result")
	public Response retrieveByExamination(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int examinationId) throws ServiceException {
		if (!userDetails.isAdmin()) {
			return new ResourceResponse(resultService.retrieveByExamination(userDetails.getId(), examinationId));
		}
		throw new AccessDeniedException();
	}
}
