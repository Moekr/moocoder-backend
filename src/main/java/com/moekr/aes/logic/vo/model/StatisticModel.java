package com.moekr.aes.logic.vo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode
@ToString
public class StatisticModel {
	private Integer studentCount;
	private Integer teacherCount;
	private Integer examinationCount;
	private Integer resultCount;
	private Integer recordCount;
	private Integer javaProblemCount;
	private Integer pythonProblemCount;
}
