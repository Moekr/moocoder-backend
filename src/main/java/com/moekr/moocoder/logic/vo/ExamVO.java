package com.moekr.moocoder.logic.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.util.enums.ExamStatus;
import com.moekr.moocoder.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class ExamVO {
	private Integer id;
	private String name;
	@JsonProperty("created_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	@JsonProperty("start_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime startAt;
	@JsonProperty("end_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime endAt;
	private ExamStatus status;
	private NestedUserVO creator;
	private boolean joined;

	public ExamVO(Exam exam) {
		BeanUtils.copyProperties(exam, this);
		this.creator = new NestedUserVO(exam.getCreator());
		if (this.status == ExamStatus.AVAILABLE) {
			LocalDateTime now = LocalDateTime.now();
			if (now.isBefore(startAt)) {
				this.status = ExamStatus.READY;
			} else if (now.isAfter(endAt)) {
				this.status = ExamStatus.FINISHED;
			}
		}
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
