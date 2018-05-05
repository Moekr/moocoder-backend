package com.moekr.moocoder.logic.vo;

import com.moekr.moocoder.data.entity.Exam;
import com.moekr.moocoder.data.entity.Problem;
import com.moekr.moocoder.data.entity.Result;
import com.moekr.moocoder.util.enums.ExamStatus;
import com.moekr.moocoder.util.enums.ProblemType;
import com.moekr.moocoder.util.enums.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class JoinedExamVO extends ExamVO {
	private String url;
	private Set<NestedProblemVO> problems;
	private NestedResultVO result;

	public JoinedExamVO(Exam exam) {
		super(exam);
		this.problems = exam.getProblems().stream().map(NestedProblemVO::new).collect(Collectors.toSet());
	}

	public JoinedExamVO(Exam exam, String url, Result result) {
		this(exam);
		this.setJoined(true);
		if (result.getOwner().getRole() == UserRole.TEACHER || this.getStatus() == ExamStatus.AVAILABLE) {
			this.url = url;
		} else {
			this.problems = null;
		}
		this.result = new NestedResultVO(result);
	}

	@Data
	private static class NestedProblemVO {
		private Integer id;
		private String name;
		private ProblemType type;
		private String description;

		NestedProblemVO(Problem problem) {
			BeanUtils.copyProperties(problem, this);
		}
	}

	@Data
	private static class NestedResultVO {
		private Integer id;
		private Integer score;

		NestedResultVO(Result result) {
			BeanUtils.copyProperties(result, this);
		}
	}
}
