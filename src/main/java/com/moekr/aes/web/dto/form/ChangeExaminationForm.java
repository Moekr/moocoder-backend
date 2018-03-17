package com.moekr.aes.web.dto.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@EqualsAndHashCode
@ToString
public class ChangeExaminationForm {
	@Pattern(regexp = "[0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}:[0-9]{2}", message = "考试开始时间格式不正确！")
	@NotBlank(message = "请填写考试开始时间！")
	private String startAt;
	@Pattern(regexp = "[0-9]{4}/[0-9]{2}/[0-9]{2} [0-9]{2}:[0-9]{2}", message = "考试结束时间格式不正确！")
	@NotBlank(message = "请填写考试结束时间！")
	private String endAt;
}
