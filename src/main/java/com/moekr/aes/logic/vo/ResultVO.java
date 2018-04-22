package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Result;
import com.moekr.aes.data.entity.User;
import com.moekr.aes.util.enums.BuildStatus;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ResultVO {
	private Integer id;
	private Integer score;
	private boolean deleted;
	private NestedExamVO exam;
	private NestedUserVO owner;
	private List<NestedRecordVO> recordList;

	public ResultVO(Result result) {
		BeanUtils.copyProperties(result, this);
		this.exam = new NestedExamVO(result.getExam());
		this.owner = new NestedUserVO(result.getOwner());
		this.recordList = result.getRecordSet().stream().map(NestedRecordVO::new).collect(Collectors.toList());
	}

	@Data
	private static class NestedExamVO {
		private Integer id;
		private String name;

		NestedExamVO(Exam exam) {
			BeanUtils.copyProperties(exam, this);
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

	@Data
	private static class NestedRecordVO {
		private Integer id;
		@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
		private LocalDateTime createdAt;
		private BuildStatus status;
		private Integer score;

		NestedRecordVO(Record record) {
			BeanUtils.copyProperties(record, this);
		}
	}
}
