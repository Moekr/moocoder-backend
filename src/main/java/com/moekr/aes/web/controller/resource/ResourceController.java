package com.moekr.aes.web.controller.resource;

import com.moekr.aes.logic.storage.StorageProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/upload")
public class ResourceController {
	private final StorageProvider storageProvider;

	@Autowired
	public ResourceController(StorageProvider storageProvider) {
		this.storageProvider = storageProvider;
	}

	@GetMapping(value = "/{file}")
	public void result(@PathVariable String file, HttpServletRequest request, HttpServletResponse response) throws IOException {
		byte[] content;
		try {
			content = storageProvider.fetch(file);
		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		String contentType = request.getServletContext().getMimeType(file);
		contentType = StringUtils.defaultString(contentType, "application/octet-stream");
		response.setContentType(contentType);
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file);
		OutputStream outputStream = response.getOutputStream();
		outputStream.write(content);
	}
}
