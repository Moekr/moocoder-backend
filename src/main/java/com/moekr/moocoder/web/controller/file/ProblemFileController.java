package com.moekr.moocoder.web.controller.file;

import com.moekr.moocoder.logic.service.ProblemService;
import com.moekr.moocoder.logic.storage.StorageProvider;
import com.moekr.moocoder.logic.vo.ProblemVO;
import com.moekr.moocoder.util.editors.DefaultNumberEditor;
import com.moekr.moocoder.util.exceptions.AccessDeniedException;
import com.moekr.moocoder.util.exceptions.EntityNotFoundException;
import com.moekr.moocoder.util.exceptions.ServiceException;
import com.moekr.moocoder.web.security.impl.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/file/problem")
public class ProblemFileController {
	private final ProblemService problemService;
	private final StorageProvider storageProvider;

	@Autowired
	public ProblemFileController(ProblemService problemService, StorageProvider storageProvider) {
		this.problemService = problemService;
		this.storageProvider = storageProvider;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(int.class, new DefaultNumberEditor(-1));
	}

	@GetMapping(value = "/{problemId:\\d+}")
	public void result(@AuthenticationPrincipal CustomUserDetails userDetails,
					   @PathVariable int problemId,
					   HttpServletResponse response) throws IOException {
		String fileName;
		byte[] content;
		try {
			ProblemVO problem = problemService.retrieve(userDetails.getId(), problemId);
			fileName = problem.getUniqueName() + ".zip";
			content = storageProvider.fetch(fileName);
		} catch (AccessDeniedException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		} catch (EntityNotFoundException | FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (ServiceException | IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		response.setContentType("application/zip");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(content);
	}
}
