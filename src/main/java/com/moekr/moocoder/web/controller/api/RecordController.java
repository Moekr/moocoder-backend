package com.moekr.moocoder.web.controller.api;

import com.moekr.moocoder.logic.service.RecordService;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.response.ResourceResponse;
import com.moekr.moocoder.web.response.Response;
import com.moekr.moocoder.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RecordController extends AbstractApiController {
	private final RecordService recordService;

	@Autowired
	public RecordController(RecordService recordService) {
		this.recordService = recordService;
	}

	@GetMapping("/record/{recordId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int recordId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(recordService.retrieve(recordId));
		} else {
			return new ResourceResponse(recordService.retrieve(userDetails.getId(), recordId));
		}
	}
}
