package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.Commit;
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
	@JsonProperty("created_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	private BuildStatus status;
	@JsonProperty("console_output")
	private String consoleOutput;
	private Integer score;
	private Set<NestedFailureVO> failures;
	private NestedCommitVO commit;

	public RecordVO(Record record) {
		BeanUtils.copyProperties(record, this, "consoleOutput");
		this.consoleOutput = convertConsoleOutput(record.getConsoleOutput());
		this.failures = record.getFailures().stream().map(NestedFailureVO::new).collect(Collectors.toSet());
		this.commit = new NestedCommitVO(record.getCommit());
	}

	private String convertConsoleOutput(String consoleOutput) {
		String beginIndicator = "==CONSOLE OUTPUT BEGIN==";
		String endIndicator = "==CONSOLE OUTPUT END==";
		int begin = consoleOutput.indexOf(beginIndicator);
		if (begin == -1) {
			begin = 0;
		} else {
			begin = begin + beginIndicator.length() + 2;
		}
		int end = consoleOutput.lastIndexOf(endIndicator);
		if (end == -1) {
			end = consoleOutput.length();
		}
		return consoleOutput.substring(begin, end);
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

	@Data
	private static class NestedCommitVO {
		private Integer id;

		NestedCommitVO(Commit commit) {
			BeanUtils.copyProperties(commit, this);
		}
	}
}
