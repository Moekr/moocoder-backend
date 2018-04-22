package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.Record;
import com.moekr.aes.data.entity.Record.Failure;
import com.moekr.aes.util.enums.BuildStatus;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class RecordVO {
	private Integer id;
	private Integer number;
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	private BuildStatus status;
	private Integer score;
	private Set<NestedFailureVO> failures;

	public RecordVO(Record record) {
		BeanUtils.copyProperties(record, this);
		this.failures = record.getFailures().stream().map(NestedFailureVO::new).collect(Collectors.toSet());
	}

	@Data
	private static class NestedFailureVO {
		private String name;
		private String details;
		private String trace;

		NestedFailureVO(Failure failure) {
			BeanUtils.copyProperties(failure, this);
		}
	}
}
