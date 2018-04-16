package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.Examination;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.serializer.CustomLocalDateTimeSerializer;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode
@ToString
public class ExaminationVO {
	private Integer id;
	private String name;
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	@JsonSerialize(using = CustomLocalDateTimeSerializer.class)
	private LocalDateTime startAt;
	@JsonSerialize(using = CustomLocalDateTimeSerializer.class)
	private LocalDateTime endAt;
	private NestedUserVO creator;
	private Set<NestedProblemVO> problems;

	public ExaminationVO(Examination examination) {
		BeanUtils.copyProperties(examination, this);
		creator = new NestedUserVO(examination.getOwner());
		problems = examination.getProblemSet().stream().map(NestedProblemVO::new).collect(Collectors.toSet());
	}

	@Data
	@EqualsAndHashCode
	@ToString
	private static class NestedUserVO {
		private Integer id;
		private String username;

		public NestedUserVO(User user) {
			BeanUtils.copyProperties(user, this);
		}
	}

	@Data
	@EqualsAndHashCode
	@ToString
	private static class NestedProblemVO {
		private Integer id;
		private String name;

		public NestedProblemVO(Problem problem) {
			BeanUtils.copyProperties(problem, this);
		}
	}
}
