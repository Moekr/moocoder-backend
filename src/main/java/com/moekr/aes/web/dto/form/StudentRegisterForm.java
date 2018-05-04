package com.moekr.aes.web.dto.form;

import com.moekr.aes.util.validate.FieldEquals;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@FieldEquals(firstField = "password", secondField = "confirm", message = "两次输入的密码不一致！")
public class StudentRegisterForm {
	@Pattern(regexp = "[0-9a-zA-Z_]{4,16}", message = "用户名只能包含大小写字母、数字和下划线，且必须为4-16位！")
	@NotNull(message = "请填写用户名！")
	private String username;
	@Email(message = "邮箱格式不正确！")
	@NotNull(message = "请填写邮箱！")
	private String email;
	@Pattern(regexp = "[0-9a-zA-Z_]{8,16}", message = "密码只能包含大小写字母、数字和下划线，且必须为8-16位！")
	@NotNull(message = "请填写密码！")
	private String password;
	private String confirm;
}
