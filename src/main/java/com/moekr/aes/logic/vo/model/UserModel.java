package com.moekr.aes.logic.vo.model;

import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.time.ZoneOffset;

@Data
@EqualsAndHashCode
@ToString
public class UserModel {
	private Integer id;
	private String username;
	private String email;
	private String role;
	private Long createdAt;
	private Integer problemCount;
	private Integer examinationCount;

	public UserModel(User user) {
		BeanUtils.copyProperties(user, this);
		this.role = user.getRole().toString();
		this.createdAt = user.getCreatedAt().toEpochSecond(ZoneOffset.ofHours(8));
		this.problemCount = user.getProblemSet().size();
		this.examinationCount = user.getRole() == Role.TEACHER ? user.getExaminationSet().size() : user.getResultSet().size();
	}
}
