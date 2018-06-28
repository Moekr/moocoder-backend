package com.moekr.moocoder.web.dto.form;

import com.moekr.moocoder.util.validate.FieldEquals;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@FieldEquals(firstField = "password", secondField = "confirm", message = "两次输入的密码不一致！")
public class ChangePasswordForm {
	@NotNull(message = "请填写原密码！")
	private String origin;
	@Pattern(regexp = "[0-9a-zA-Z_]{8,16}", message = "密码只能包含大小写字母、数字和下划线，且必须为8-16位！")
	@NotNull(message = "请填写新密码！")
	private String password;
	private String confirm;
}
