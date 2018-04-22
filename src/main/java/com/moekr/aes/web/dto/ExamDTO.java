package com.moekr.aes.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.moekr.aes.util.serializer.CustomLocalDateTimeDeserializer;
import com.moekr.aes.util.validate.FieldCompare;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@FieldCompare.List({
		@FieldCompare(lessField = "startAt", greaterField = "endAt", message = "考试考试时间必须早于考试结束时间！", groups = PostMapping.class),
		@FieldCompare(lessField = "now", greaterField = "endAt", message = "考试考试时间必须早于考试结束时间！", groups = PostMapping.class)
})
public class ExamDTO {
	@NotBlank(message = "请填写考试名称！", groups = {PostMapping.class, PutMapping.class})
	private String name;
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
	@NotNull(message = "请填写考试开始时间！", groups = {PostMapping.class, PutMapping.class})
	private LocalDateTime startAt;
	@JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
	@NotNull(message = "请填写考试结束时间！", groups = {PostMapping.class, PutMapping.class})
	private LocalDateTime endAt;
	@NotEmpty(message = "请提供题目列表！", groups = PostMapping.class)
	private Set<Integer> problemSet;
	// 用于比较考试结束时间
	private final LocalDateTime now = LocalDateTime.now();
}
