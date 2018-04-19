package com.moekr.aes.logic.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.moekr.aes.data.entity.Problem;
import com.moekr.aes.util.enums.ProblemType;
import com.moekr.aes.util.serializer.TimestampLocalDateTimeSerializer;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProblemVO {
	private Integer id;
	private String name;
	private ProblemType type;
	private String description;
	private Set<String> publicFiles;
	private Set<String> protectedFiles;
	private Set<String> privateFiles;
	@JsonSerialize(using = TimestampLocalDateTimeSerializer.class)
	private LocalDateTime createdAt;
	private Boolean deprecated;

	public ProblemVO(Problem problem) {
		BeanUtils.copyProperties(problem, this);
	}
}
