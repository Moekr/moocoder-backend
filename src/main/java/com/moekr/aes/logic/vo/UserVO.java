package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.UserRole;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class UserVO {
	private Integer id;
	private String username;
	private String email;
	private UserRole role;
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;

	public UserVO(User user) {
		BeanUtils.copyProperties(user, this);
	}
}
