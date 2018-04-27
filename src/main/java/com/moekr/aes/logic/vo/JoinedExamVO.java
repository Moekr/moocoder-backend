package com.moekr.aes.logic.vo;

import com.moekr.aes.data.entity.Exam;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.util.enums.ExamStatus;
import com.moekr.aes.util.enums.ProblemType;
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

	public JoinedExamVO(Exam exam) {
		super(exam);
		this.setJoined(true);
		this.problems = exam.getProblems().stream().map(NestedProblemVO::new).collect(Collectors.toSet());
	}

	public JoinedExamVO(Exam exam, String url) {
		this(exam);
		if (this.getStatus() == ExamStatus.AVAILABLE) {
			this.url = url;
		}
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
}
