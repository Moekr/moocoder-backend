package com.moekr.moocoder.logic.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.util.enums.UserRole;
import com.moekr.moocoder.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class UserVO {
	private Integer id;
	private String username;
	private String email;
	private UserRole role;
	@JsonProperty("created_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;

	private UserVO() {
		this.id = 0;
		this.username = "管理员";
	}

	public UserVO(User user) {
		BeanUtils.copyProperties(user, this);
	}

	public static final UserVO ADMIN = new UserVO();
}
