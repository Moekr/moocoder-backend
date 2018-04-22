package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ExamVO {
	private Integer id;
	private String name;
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime startAt;
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime endAt;
	private NestedUserVO creator;
	private Set<NestedProblemVO> problems;

	public ExamVO(Exam exam) {
		BeanUtils.copyProperties(exam, this);
		creator = new NestedUserVO(exam.getCreator());
		problems = exam.getProblemSet().stream().map(NestedProblemVO::new).collect(Collectors.toSet());
	}

	@Data
	private static class NestedUserVO {
		private Integer id;
		private String username;

		NestedUserVO(User user) {
			BeanUtils.copyProperties(user, this);
		}
	}

	@Data
	private static class NestedProblemVO {
		private Integer id;
		private String name;

		NestedProblemVO(Problem problem) {
			BeanUtils.copyProperties(problem, this);
		}
	}
}
