package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.RecordService;
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
public class RecordController extends AbstractApiController {
	private final RecordService recordService;

	public RecordController(RecordService recordService) {
		this.recordService = recordService;
	}

	@GetMapping("/record/{recordId:\\d+}")
	public Map<String, Object> retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
										@PathVariable int recordId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return ToolKit.assemblyResponseBody(recordService.retrieve(recordId));
		} else {
			return ToolKit.assemblyResponseBody(recordService.retrieve(userDetails.getId(), recordId));
		}
	}
}
