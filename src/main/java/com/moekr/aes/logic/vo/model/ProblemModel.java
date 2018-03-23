package com.moekr.aes.logic.vo.model;

import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.util.enums.Language;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.time.ZoneId;

@Data
@EqualsAndHashCode
@ToString
public class ProblemModel {
	private Integer id;
	private String name;
	private Language language;
	private String file;
	private Long createdAt;
	private Boolean deprecated;
	private Integer usedTime;

	public ProblemModel(Problem problem) {
		BeanUtils.copyProperties(problem, this);
		this.createdAt = problem.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond();
		this.usedTime = problem.getExaminationSet().size();
	}
}
