package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.CommitService;
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
public class CommitController extends AbstractApiController {
	private final CommitService commitService;

	@Autowired
	public CommitController(CommitService commitService) {
		this.commitService = commitService;
	}

	@GetMapping("/commit/{commitId:\\d+}")
	public Response retrieve(@AuthenticationPrincipal CustomUserDetails userDetails,
							 @PathVariable int commitId) throws ServiceException {
		if (userDetails.isAdmin()) {
			return new ResourceResponse(commitService.retrieve(commitId));
		} else {
			return new ResourceResponse(commitService.retrieve(userDetails.getId(), commitId));
		}
	}
}
