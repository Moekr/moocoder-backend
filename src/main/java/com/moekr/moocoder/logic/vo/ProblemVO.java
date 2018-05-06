package com.moekr.moocoder.logic.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.util.enums.ProblemType;
import com.moekr.moocoder.util.serializer.TimestampLocalDateTimeSerializer;
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
	@JsonProperty("modified_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime modifiedAt;
	private boolean deprecated;
	private NestedUserVO creator;

	public ProblemVO(Problem problem) {
		BeanUtils.copyProperties(problem, this);
		this.creator = problem.getCreator() == null ? null : new NestedUserVO(problem.getCreator());
	}

	@JsonIgnore
	public String getUniqueName() {
		return (id + "-" + name).toLowerCase();
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
