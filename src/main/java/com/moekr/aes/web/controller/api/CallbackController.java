package com.moekr.aes.web.controller.api;

import com.moekr.aes.logic.service.RecordService;
import com.moekr.aes.util.AesProperties;
import com.moekr.aes.util.Asserts;
import com.moekr.aes.util.ToolKit;
import com.moekr.aes.web.dto.form.CallbackForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/callback")
public class CallbackController {
	private final RecordService recordService;
	private final String secret;

	@Autowired
	public CallbackController(RecordService recordService, AesProperties properties) {
		this.recordService = recordService;
		this.secret = properties.getLocal().getSecret();
	}

	@PostMapping("/{id}")
	public Map<String, Object> callback(@ModelAttribute @Valid CallbackForm form, Errors errors, @PathVariable String id) {
		Asserts.isTrue(!errors.hasFieldErrors(), HttpStatus.SC_BAD_REQUEST, errors.getFieldError().getDefaultMessage());
		Asserts.isTrue(StringUtils.equals(this.secret, form.getSecret()), HttpStatus.SC_FORBIDDEN, "认证失败！");
		Asserts.isTrue(StringUtils.isNumeric(id), HttpStatus.SC_BAD_REQUEST, "ID格式不正确！");
		recordService.asyncRecord(Integer.valueOf(id), form.getBuildNumber());
		return ToolKit.emptyResponseBody();
	}
}
