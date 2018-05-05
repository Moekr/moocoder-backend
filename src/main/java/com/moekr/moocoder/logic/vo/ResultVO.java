package com.moekr.moocoder.logic.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.moocoder.data.entity.Commit;
import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.data.entity.User;
import com.moekr.moocoder.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ResultVO {
	private Integer id;
	private Integer score;
	@JsonProperty("last_commit_at")
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime lastCommitAt;
	private boolean deleted;
	private NestedExamVO exam;
	private NestedUserVO owner;
	private List<NestedCommitVO> commits;

	public ResultVO(Result result, boolean withCommits) {
		BeanUtils.copyProperties(result, this);
		this.exam = new NestedExamVO(result.getExam());
		this.owner = new NestedUserVO(result.getOwner());
		if (withCommits) {
			this.commits = result.getCommitList().stream().map(NestedCommitVO::new).collect(Collectors.toList());
		}
	}

	// 提供给ResultFileController获取内部用户名
	public String getUsername() {
		return owner.username;
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
	private static class NestedCommitVO {
		private Integer id;
		private boolean finished;
		private Integer score;
		@JsonProperty("created_at")
		@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
		private LocalDateTime createdAt;

		NestedCommitVO(Commit commit) {
			BeanUtils.copyProperties(commit, this);
		}
	}
}
