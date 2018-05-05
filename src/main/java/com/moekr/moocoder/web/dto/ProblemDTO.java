package com.moekr.moocoder.web.dto;

import com.moekr.moocoder.util.enums.ProblemType;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ProblemDTO {
	@Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "题目名称只能包含大小写字母、数字、下划线和连字符！", groups = PostMapping.class)
	@NotNull(message = "题目名称不能为空！", groups = PostMapping.class)
	private String name;
	@NotNull(message = "题目类型不能为空！", groups = PostMapping.class)
	private ProblemType type;
	@NotNull(message = "题目描述不能为空！", groups = PostMapping.class)
	private String description;
}
