package com.moekr.aes.web.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.PutMapping;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class ProblemDTO {
	@NotNull(message = "请提供可修改文件列表！", groups = PutMapping.class)
	private Set<String> publicFiles;
	@NotNull(message = "请提供不可修改文件列表！", groups = PutMapping.class)
	private Set<String> protectedFiles;
	@NotNull(message = "请提供不可见文件列表！", groups = PutMapping.class)
	private Set<String> privateFiles;
}
