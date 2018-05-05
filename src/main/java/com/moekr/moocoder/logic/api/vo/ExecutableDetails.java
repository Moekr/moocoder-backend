package com.moekr.moocoder.logic.api.vo;

import com.offbytwo.jenkins.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExecutableDetails extends BaseModel {
	private Long number;
	private String url;
}
