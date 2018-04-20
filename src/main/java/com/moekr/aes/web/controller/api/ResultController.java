package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.ResultService;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.util.exceptions.ServiceException;
import com.moekr.aes.web.security.impl.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ResultController extends AbstractApiController {
	private final ResultService resultService;

	public ResultController(ResultService resultService) {
		this.resultService = resultService;
	}

	@GetMapping("/result/{resultId:\\d+}")
	public Map<String, Object> retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
										@PathVariable int resultId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(resultService.retrieve(resultId));
		} else {
			return ToolKit.assemblyResponseBody(resultService.retrieve(userDetails.getId(), resultId));
		}
	}
}
