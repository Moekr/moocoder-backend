package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.*;
import com.moekr.aes.util.enums.BuildStatus;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class CommitVO {
	private Integer id;
	private String hash;
	private boolean finished;
	private Integer score;
	@JsonProperty("created_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	private NestedResultVO result;
	private Set<NestedRecordVO> records;

	public CommitVO(Commit commit) {
		BeanUtils.copyProperties(commit, this);
		this.result = new NestedResultVO(commit.getResult());
		this.records = commit.getRecords().stream().map(NestedRecordVO::new).collect(Collectors.toSet());
	}

	@Data
	private static class NestedRecordVO {
		private Integer id;
		private BuildStatus status;
		private Integer score;
		private NestedProblemVO problem;

		NestedRecordVO(Record record) {
			BeanUtils.copyProperties(record, this);
			this.problem = new NestedProblemVO(record.getProblem());
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

	@Data
	private static class NestedResultVO {
		private Integer id;
		private Integer score;
		private NestedExamVO exam;

		NestedResultVO(Result result) {
			BeanUtils.copyProperties(result, this);
			this.exam = new NestedExamVO(result.getExam());
		}

		@Data
		private static class NestedExamVO {
			private Integer id;
			private String name;

			NestedExamVO(Exam exam) {
				BeanUtils.copyProperties(exam, this);
			}
		}
	}
}
