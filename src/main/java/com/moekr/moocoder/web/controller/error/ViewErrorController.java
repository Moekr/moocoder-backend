package com.moekr.moocoder.web.controller.error;

import com.moekr.moocoder.util.ToolKit;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(GlobalErrorController.ERROR_PATH)
public class ViewErrorController {
	@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
	public String error(Model model, HttpServletRequest request) {
		HttpStatus status = ToolKit.httpStatus(request);
		model.addAttribute("error", status.value());
		model.addAttribute("message", status.getReasonPhrase());
		return "error";
	}
}
