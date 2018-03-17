package com.moekr.aes.web.dto.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode
@ToString
public class CallbackForm {
	@NotNull(message = "请求格式不正确！")
	private Integer buildNumber;
	@NotNull(message = "请求格式不正确！")
	private String secret;
}
