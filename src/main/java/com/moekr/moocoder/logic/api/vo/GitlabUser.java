package com.moekr.moocoder.logic.api.vo;

import lombok.Data;

@Data
public class GitlabUser {
	private Integer id;
	private Integer namespace;
	private String token;
}
