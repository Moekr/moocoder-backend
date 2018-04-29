package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.ProblemType;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProblemVO {
	private Integer id;
	private String name;
	private ProblemType type;
	private String description;
	@JsonProperty("public_files")
	private Set<String> publicFiles;
	@JsonProperty("protected_files")
	private Set<String> protectedFiles;
	@JsonProperty("private_files")
	private Set<String> privateFiles;
	@JsonProperty("created_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	private boolean deprecated;
	private NestedUserVO creator;

	public ProblemVO(Problem problem) {
		BeanUtils.copyProperties(problem, this);
		this.creator = problem.getCreator() == null ? null : new NestedUserVO(problem.getCreator());
	}

	@Data
	private static class NestedUserVO {
		private Integer id;
		private String username;

		NestedUserVO(User user) {
			BeanUtils.copyProperties(user, this);
		}
	}
}
