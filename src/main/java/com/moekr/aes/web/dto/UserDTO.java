package com.moekr.aes.web.dto;

import com.moekr.aes.util.enums.UserRole;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UserDTO {
	@Pattern(regexp = "[0-9a-zA-Z_]{4,16}", message = "用户名只能包含大小写字母、数字和下划线，且必须为4-16位！", groups = PostMapping.class)
	@NotNull(message = "请填写用户名！", groups = PostMapping.class)
	private String username;
	@Email(message = "邮箱格式不正确！", groups = {PostMapping.class, PutMapping.class})
	@NotNull(message = "请填写邮箱！", groups = {PostMapping.class, PutMapping.class})
	private String email;
	@NotNull(message = "请选择用户角色！", groups = PostMapping.class)
	private UserRole role;
}
